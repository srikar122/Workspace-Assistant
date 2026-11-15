package com.org.workspace.service;

public interface EmailService {
    void sendEmail(String to, String subject, String bodyText);
}     