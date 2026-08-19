package com.fedu.fedu.config;

import com.fedu.fedu.entity.SystemSetting;
import com.fedu.fedu.repository.SystemSettingRepository;
import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class DynamicJavaMailSender implements JavaMailSender {

    private final SystemSettingRepository systemSettingRepository;
    private JavaMailSenderImpl delegate;

    @PostConstruct
    public void init() {
        reloadConfiguration();
    }

    public void reloadConfiguration() {
        log.info("Reloading Mail Configuration from Database...");
        JavaMailSenderImpl newSender = new JavaMailSenderImpl();
        
        Optional<SystemSetting> serverOpt = systemSettingRepository.findById("MAIL_SERVER");
        Optional<SystemSetting> portOpt = systemSettingRepository.findById("MAIL_PORT");
        Optional<SystemSetting> usernameOpt = systemSettingRepository.findById("MAIL_USERNAME");
        Optional<SystemSetting> passwordOpt = systemSettingRepository.findById("MAIL_PASSWORD");
        Optional<SystemSetting> securityOpt = systemSettingRepository.findById("MAIL_SECURITY");

        if (serverOpt.isPresent() && usernameOpt.isPresent()) {
            newSender.setHost(serverOpt.get().getSettingValue());
            newSender.setPort(Integer.parseInt(portOpt.map(SystemSetting::getSettingValue).orElse("587")));
            newSender.setUsername(usernameOpt.get().getSettingValue());
            newSender.setPassword(passwordOpt.get().getSettingValue());

            Properties props = newSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            
            String security = securityOpt.map(SystemSetting::getSettingValue).orElse("TLS");
            if ("TLS".equalsIgnoreCase(security)) {
                props.put("mail.smtp.starttls.enable", "true");
            } else if ("SSL".equalsIgnoreCase(security)) {
                props.put("mail.smtp.ssl.enable", "true");
            }
        } else {
            // Default fallback if DB is empty
            log.warn("Database mail config is empty, using fallback");
            newSender.setHost("smtp.gmail.com");
            newSender.setPort(587);
            newSender.setUsername("default");
            newSender.setPassword("default");
        }
        
        this.delegate = newSender;
        log.info("Mail Configuration Reloaded. Host: {}", delegate.getHost());
    }

    @Override
    public MimeMessage createMimeMessage() {
        return delegate.createMimeMessage();
    }

    @Override
    public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
        return delegate.createMimeMessage(contentStream);
    }

    @Override
    public void send(MimeMessage mimeMessage) throws MailException {
        delegate.send(mimeMessage);
    }

    @Override
    public void send(MimeMessage... mimeMessages) throws MailException {
        delegate.send(mimeMessages);
    }

    @Override
    public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
        delegate.send(mimeMessagePreparator);
    }

    @Override
    public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException {
        delegate.send(mimeMessagePreparators);
    }

    @Override
    public void send(SimpleMailMessage simpleMessage) throws MailException {
        delegate.send(simpleMessage);
    }

    @Override
    public void send(SimpleMailMessage... simpleMessages) throws MailException {
        delegate.send(simpleMessages);
    }
}
