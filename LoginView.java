package com.college.db;

import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;

@Route("login")
@PageTitle("Admin Login")
public class LoginView extends VerticalLayout {
    public LoginView(AuthService auth) {
        TextField user = new TextField("👤 User ID");
        user.setValue("admin");
        user.setReadOnly(true);
        PasswordField pass = new PasswordField("🔑 Password");
        Button go = new Button("🔓 Verify & Access System", e -> {
            if (auth.verify(user.getValue(), pass.getValue())) {
                VaadinSession.getCurrent().setAttribute("user", "admin");
                getUI().ifPresent(ui -> ui.navigate(""));
            } else Notification.show("❌ गलत पासवर्ड दर्ज किया गया है!");
        });
        go.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        setAlignItems(Alignment.CENTER);
        add(new H2("🔒 Permanent Shared Live Database"), user, pass, go);
    }
}
