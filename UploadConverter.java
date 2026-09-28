package com.college.db;

import org.apache.commons.csv.*;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.util.*;

/** convert_excel_to_csv_bytes() + _clean_raw_table() + _best_frame() का Java रूप.
 *  फ़ाइल का प्रकार एक्सटेंशन से नहीं, अंदर की सामग्री (magic bytes) से पहचाना जाता है. */
public final class UploadConverter {
    private UploadConverter() {}

    public static List<Map<String, String>> read(byte[] raw) throws IOException {
        List<List<List<String>>> frames = new ArrayList<>();
        boolean zip = raw.length > 4 && raw[0] == 'P' && raw[1] == 'K';
        boolean ole = raw.length > 8 && (raw[0] & 0xFF) == 0xD0 && (raw[1] & 0xFF) == 0xCF;
        if (zip || ole) {
            DataFormatter fmt = new DataFormatter();
            try (Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(raw))) {
                for (Sheet sh : wb) {
                    List<List<String>> t = new ArrayList<>();
                    for (Row r : sh) {
                        List<String> cells = new ArrayList<>();
                        for (int i = 0; i < Math.max(r.getLastCellNum(), 0); i++) {
                            Cell c = r.getCell(i);
                            cells.add(c == null ? "" : fmt.formatCellValue(c));
                        }
                        t.add(cells);
                    }
                    frames.add(t);
                }
            }
        } else {
            frames.add(parseDelimited(decode(raw)));
        }
        List<Map<String, String>> best = List.of();
        long bestCells = 0;
        for (List<List<String>> f : frames) {
            List<Map<String, String>> cleaned = clean(f);
            long cells = cleaned.isEmpty() ? 0 : (long) cleaned.size() * cleaned.get(0).size();
            if (cells > bestCells) { best = cleaned; bestCells = cells; }
        }
        return best;
    }

    private static String decode(byte[] raw) {
        try {
            String s = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(raw)).toString();
            return s.startsWith("\uFEFF") ? s.substring(1) : s;
        } catch (CharacterCodingException e) {
            return new String(raw, Charset.forName("windows-1252"));
        }
    }

    private static List<List<String>> parseDelimited(String text) throws IOException {
        String[] lines = text.split("\r?\n", 21);
        char best = ',';
        int max = -1;
        for (char sep : new char[]{'\t', ',', ';', '|'}) {
            int n = 0;
            for (int i = 0; i < Math.min(20, lines.length); i++) for (char ch : lines[i].toCharArray()) if (ch == sep) n++;
            if (n > max) { max = n; best = sep; }
        }
        List<List<String>> rows = new ArrayList<>();
        try (CSVParser p = CSVFormat.DEFAULT.builder().setDelimiter(best).setIgnoreEmptyLines(true).build().parse(new StringReader(text))) {
            for (CSVRecord r : p) { List<String> row = new ArrayList<>(); r.forEach(row::add); rows.add(row); }
        }
        return rows;
    }

    /** खाली rows/columns हटाओ, ऊपर के title-rows छोड़ो, सही header row ढूँढो. */
    private static List<Map<String, String>> clean(List<List<String>> raw) {
        int w = 0;
        for (List<String> r : raw) w = Math.max(w, r.size());
        List<List<String>> rows = new ArrayList<>();
        for (List<String> r : raw) {
            List<String> n = new ArrayList<>();
            for (int i = 0; i < w; i++) n.add(i < r.size() && r.get(i) != null ? r.get(i).trim().replaceAll("\\s00:00:00$", "") : "");
            if (n.stream().anyMatch(s -> !s.isEmpty())) rows.add(n);
        }
        List<Integer> keep = new ArrayList<>();
        for (int c = 0; c < w; c++) { final int cc = c; if (rows.stream().anyMatch(r -> !r.get(cc).isEmpty())) keep.add(c); }
        if (rows.isEmpty() || keep.isEmpty()) return List.of();
        int[] counts = new int[rows.size()];
        int maxCount = 0;
        for (int i = 0; i < rows.size(); i++) {
            for (int c : keep) if (!rows.get(i).get(c).isEmpty()) counts[i]++;
            maxCount = Math.max(maxCount, counts[i]);
        }
        int hdr = 0;
        for (int i = 0; i < counts.length; i++) if (counts[i] >= Math.max(1, maxCount * 0.5)) { hdr = i; break; }
        List<String> header = new ArrayList<>();
        for (int k = 0; k < keep.size(); k++) { String h = rows.get(hdr).get(keep.get(k)); header.add(h.isEmpty() ? "Unnamed_" + k : h); }
        List<Map<String, String>> out = new ArrayList<>();
        for (int i = hdr + 1; i < rows.size(); i++) {
            Map<String, String> m = new LinkedHashMap<>();
            for (int k = 0; k < keep.size(); k++) m.put(header.get(k), rows.get(i).get(keep.get(k)));
            out.add(m);
        }
        return out;
    }
}
