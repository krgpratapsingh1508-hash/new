package com.college.db;

import org.apache.commons.csv.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** load_live_data() / save_live_data() का Java रूप (CSV फ़ाइल = डेटाबेस). */
@Service
public class DataService {
    private static final Path DB = Path.of("shared_student_database.csv");
    private List<Map<String, String>> cache;

    public synchronized List<Map<String, String>> load() {
        if (cache == null) cache = read();
        List<Map<String, String>> copy = new ArrayList<>();
        for (Map<String, String> r : cache) copy.add(new LinkedHashMap<>(r));
        return copy;
    }

    public synchronized void save(List<Map<String, String>> rows) {
        List<Map<String, String>> out = new ArrayList<>();
        for (Map<String, String> r : rows) {
            Map<String, String> n = new LinkedHashMap<>();
            for (String c : Columns.DEFAULT) n.put(c, r.getOrDefault(c, ""));   // "नो न्यू कॉलम" पॉलिसी
            twinSync(n, true);
            out.add(n);
        }
        try (Writer w = Files.newBufferedWriter(DB, StandardCharsets.UTF_8);
             CSVPrinter p = new CSVPrinter(w, CSVFormat.DEFAULT.builder().setHeader(Columns.DEFAULT.toArray(new String[0])).build())) {
            for (Map<String, String> r : out) {
                List<String> vals = new ArrayList<>();
                for (String c : Columns.DEFAULT) vals.add(r.get(c));
                p.printRecord(vals);
            }
        } catch (IOException e) { throw new UncheckedIOException(e); }
        cache = out;
    }

    private List<Map<String, String>> read() {
        List<Map<String, String>> rows = new ArrayList<>();
        try {
            if (!Files.exists(DB) || Files.size(DB) == 0) { save(rows); return rows; }
            try (Reader rd = Files.newBufferedReader(DB, StandardCharsets.UTF_8);
                 CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(rd)) {
                for (CSVRecord rec : parser) {
                    Map<String, String> row = new LinkedHashMap<>();
                    for (String c : Columns.DEFAULT) row.put(c, rec.isMapped(c) && rec.isSet(c) ? rec.get(c).trim() : "");
                    for (String c : Columns.NAME_CASE) row.put(c, properCase(row.get(c)));
                    twinSync(row, false);
                    rows.add(row);
                }
            }
        } catch (IOException e) { throw new UncheckedIOException(e); }
        return rows;
    }

    static String properCase(String s) {
        if (s == null || s.isBlank() || s.equalsIgnoreCase("nan")) return s == null ? "" : s;
        StringBuilder sb = new StringBuilder();
        for (String w : s.trim().split(" ", -1)) {
            if (sb.length() > 0) sb.append(' ');
            if (!w.isEmpty()) sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    /** Twin Sync: load पर सिर्फ़ खाली भरता है; save पर source को target में कॉपी भी करता है. */
    static void twinSync(Map<String, String> row, boolean onSave) {
        Columns.TWINS.forEach((src, tgt) -> {
            String s = row.getOrDefault(src, "").trim(), t = row.getOrDefault(tgt, "").trim();
            if (!s.isEmpty() && (onSave ? !s.equals(t) : t.isEmpty())) row.put(tgt, s);
            else if (!t.isEmpty() && s.isEmpty()) row.put(src, t);
        });
    }
}
