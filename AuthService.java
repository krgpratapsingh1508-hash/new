package com.college.db;

import com.fasterxml.jackson.databind.*;
import org.springframework.stereotype.Service;

import java.nio.file.*;

/** अब सिर्फ़ एक लॉगिन: admin. (पासवर्ड user_credentials_v15.json में admin.password से, वरना डिफ़ॉल्ट) */
@Service
public class AuthService {
    private static final Path CRED = Path.of("user_credentials_v15.json");
    private static final String DEFAULT_PASSWORD = "admin15master";

    public boolean verify(String user, String password) {
        if (!"admin".equals(user)) return false;
        String expected = DEFAULT_PASSWORD;
        try {
            if (Files.exists(CRED)) {
                JsonNode p = new ObjectMapper().readTree(Files.readString(CRED)).path("admin").path("password");
                if (p.isTextual() && !p.asText().isEmpty()) expected = p.asText();
            }
        } catch (Exception ignored) {}
        return expected.equals(password);
    }
}
