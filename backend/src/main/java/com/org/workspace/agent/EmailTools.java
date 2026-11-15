package com.org.workspace.agent;

import com.org.workspace.service.EmailService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailTools {

    private final EmailService emailService;

    @Tool("Sends an email notification to a recipient. Use this when notifying attendees about rescheduled or cancelled meetings, or sending follow-ups.")
    public String sendEmail(
            @P("Recipient email address") String to,
            @P("Email subject line") String subject,
            @P("Full body text of the email") String body) {
        try {
            log.info("[AI Tool Executed] sendEmail to: {}", to);
            emailService.sendEmail(to, subject, body);
            return "SUCCESS: Email sent to " + to;
        } catch (Exception e) {
            return "FAILED to send email: " + e.getMessage();
        }
    }
}