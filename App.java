package com.college.db;

import com.vaadin.flow.component.page.AppShellConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App implements AppShellConfigurator {
    public static void main(String[] args) { SpringApplication.run(App.class, args); }
}
