package com.college.db;

import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;

import java.util.*;

@Route("")
@PageTitle("Permanent Shared Live Database")
public class MainView extends VerticalLayout implements BeforeEnterObserver {
    private final DataService data;
    private final Grid<Map<String, String>> liveGrid = new Grid<>();
    private final Grid<Map<String, String>> previewGrid = new Grid<>();
    private List<Map<String, String>> pending = List.of();

    public MainView(DataService data) {
        this.data = data;
        Button logout = new Button("🔒 Secure Logout", e -> {
            VaadinSession.getCurrent().setAttribute("user", null);
            getUI().ifPresent(ui -> ui.navigate("login"));
        });
        add(new HorizontalLayout(new H2("🏛️ Permanent Shared Live Database System"), logout));

        MemoryBuffer buf = new MemoryBuffer();
        Upload up = new Upload(buf);
        up.setAcceptedFileTypes(".csv", ".xls", ".xlsx", ".txt");
        up.setMaxFileSize(50 * 1024 * 1024);
        Button append = new Button("💾 डेटाबेस में जोड़ें", e -> {
            if (pending.isEmpty()) { Notification.show("पहले फ़ाइल अपलोड करें"); return; }
            List<Map<String, String>> all = data.load();
            all.addAll(pending);
            data.save(all);
            Notification.show("✅ " + pending.size() + " रिकॉर्ड जोड़े गए");
            pending = List.of();
            fill(previewGrid, pending);
            fill(liveGrid, data.load());
        });
        append.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        up.addSucceededListener(ev -> {
            try {
                pending = UploadConverter.read(buf.getInputStream().readAllBytes());
                fill(previewGrid, pending);
                Notification.show("📄 " + pending.size() + " पंक्तियाँ पढ़ी गईं");
            } catch (Exception ex) { Notification.show("❌ फ़ाइल पढ़ने में समस्या: " + ex.getMessage()); }
        });
        add(new H3("📥 Excel / CSV अपलोड"), up, previewGrid, append, new H3("📋 Live Database"), liveGrid);
        fill(liveGrid, data.load());
    }

    private static void fill(Grid<Map<String, String>> g, List<Map<String, String>> rows) {
        g.removeAllColumns();
        List<String> cols = rows.isEmpty() ? Columns.DEFAULT : new ArrayList<>(rows.get(0).keySet());
        for (String c : cols) g.addColumn(m -> m.getOrDefault(c, "")).setHeader(c).setResizable(true).setAutoWidth(true);
        g.setItems(rows);
        g.setHeight("320px");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent e) {
        if (VaadinSession.getCurrent().getAttribute("user") == null) e.forwardTo(LoginView.class);
    }
}
