package com.fedu.fedu.service;

import com.fedu.fedu.config.DynamicJavaMailSender;
import com.fedu.fedu.dto.req.EmailConfigDTO;
import com.fedu.fedu.entity.SystemSetting;
import com.fedu.fedu.repository.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemSettingRepository systemSettingRepository;
    private final DynamicJavaMailSender dynamicJavaMailSender;

    @Transactional
    public void saveEmailConfig(EmailConfigDTO configDTO) {
        saveSetting("MAIL_PROVIDER", configDTO.getProvider());
        saveSetting("MAIL_SERVER", configDTO.getServer());
        saveSetting("MAIL_SENDER_NAME", configDTO.getSenderName());
        saveSetting("MAIL_SENDER_EMAIL", configDTO.getSenderEmail());
        saveSetting("MAIL_USERNAME", configDTO.getSenderEmail());
        saveSetting("MAIL_PASSWORD", configDTO.getAppPassword());
        saveSetting("MAIL_PORT", configDTO.getPort());
        saveSetting("MAIL_SECURITY", configDTO.getSecurity());

        // Trigger reload
        dynamicJavaMailSender.reloadConfiguration();
        log.info("Email configuration saved and reloaded successfully.");
    }

    public EmailConfigDTO getEmailConfig() {
        EmailConfigDTO dto = new EmailConfigDTO();
        dto.setProvider(getSettingValue("MAIL_PROVIDER", "Gmail"));
        dto.setServer(getSettingValue("MAIL_SERVER", "smtp.gmail.com"));
        dto.setSenderName(getSettingValue("MAIL_SENDER_NAME", ""));
        dto.setSenderEmail(getSettingValue("MAIL_SENDER_EMAIL", ""));
        dto.setAppPassword(getSettingValue("MAIL_PASSWORD", ""));
        dto.setPort(getSettingValue("MAIL_PORT", "587"));
        dto.setSecurity(getSettingValue("MAIL_SECURITY", "TLS"));
        return dto;
    }

    public void testEmailConfig(EmailConfigDTO configDTO) throws Exception {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(configDTO.getServer());
        mailSender.setPort(Integer.parseInt(configDTO.getPort()));
        mailSender.setUsername(configDTO.getSenderEmail());
        mailSender.setPassword(configDTO.getAppPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        
        if ("TLS".equalsIgnoreCase(configDTO.getSecurity())) {
            props.put("mail.smtp.starttls.enable", "true");
        } else if ("SSL".equalsIgnoreCase(configDTO.getSecurity())) {
            props.put("mail.smtp.ssl.enable", "true");
        }
        
        // This will throw MessagingException if connection fails
        mailSender.testConnection();
    }

    private void saveSetting(String key, String value) {
        if (value == null) return;
        SystemSetting setting = systemSettingRepository.findById(key)
                .orElse(SystemSetting.builder().settingKey(key).build());
        setting.setSettingValue(value);
        systemSettingRepository.save(setting);
    }
    
    public String getSettingValue(String key, String defaultValue) {
        return systemSettingRepository.findById(key)
                .map(SystemSetting::getSettingValue)
                .orElse(defaultValue);
    }
}
