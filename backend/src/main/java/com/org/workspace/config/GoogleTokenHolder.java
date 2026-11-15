package com.org.workspace.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class GoogleTokenHolder {

    private String accessToken;
    private String userEmail;
    private String userName;

    public boolean isConnected() {
        return accessToken != null && !accessToken.isBlank();
    }

    public void clear() {
        this.accessToken = null;
        this.userEmail = null;
        this.userName = null;
    }
}