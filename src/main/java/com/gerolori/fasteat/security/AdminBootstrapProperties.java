package com.gerolori.fasteat.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fasteat.security.admin-bootstrap")
public class AdminBootstrapProperties {

    private List<String> adminEmails = new ArrayList<>();
    private boolean createMissingUsers;
    private String defaultPassword = "";

    public List<String> getAdminEmails() {
        return adminEmails;
    }

    public void setAdminEmails(List<String> adminEmails) {
        this.adminEmails = adminEmails;
    }

    public boolean isCreateMissingUsers() {
        return createMissingUsers;
    }

    public void setCreateMissingUsers(boolean createMissingUsers) {
        this.createMissingUsers = createMissingUsers;
    }

    public String getDefaultPassword() {
        return defaultPassword;
    }

    public void setDefaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword;
    }

    public Set<String> normalizedAdminEmails() {
        return adminEmails.stream()
                .map(email -> email == null ? "" : email.trim().toLowerCase(Locale.ROOT))
                .filter(email -> !email.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }
}
