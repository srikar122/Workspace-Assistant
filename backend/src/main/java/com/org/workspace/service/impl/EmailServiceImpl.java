package com.org.workspace.service.impl;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.org.workspace.common.exception.BusinessException;
import com.org.workspace.config.GoogleTokenHolder;
import com.org.workspace.service.EmailService;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final GoogleTokenHolder tokenHolder;

    @Override
    public void sendEmail(String to, String subject, String bodyText) {
        if (!tokenHolder.isConnected()) {
            log.info("[LOCAL SIMULATION - Google Not Connected] Sending Email -> To: {} | Subject: {} | Body: {}", to, subject, bodyText);
            return;
        }

        try {
            GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(tokenHolder.getAccessToken(), null));
            Gmail gmail = new Gmail.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName("Enterprise Executive Workspace")
                    .build();

            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);
            MimeMessage email = new MimeMessage(session);
            email.setFrom(new InternetAddress("me"));
            email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
            email.setSubject(subject);
            email.setText(bodyText);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            email.writeTo(buffer);
            String encodedEmail = Base64.getUrlEncoder().encodeToString(buffer.toByteArray());

            Message message = new Message().setRaw(encodedEmail);
            gmail.users().messages().send("me", message).execute();
            log.info("Successfully sent live Gmail message to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email via Gmail API", e);
            throw new BusinessException("Failed to send email: " + e.getMessage());
        }
    }
}