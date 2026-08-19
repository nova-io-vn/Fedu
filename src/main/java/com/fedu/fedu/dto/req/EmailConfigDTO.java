package com.fedu.fedu.dto.req;

import lombok.Data;

@Data
public class EmailConfigDTO {
    private String provider;
    private String server;
    private String senderName;
    private String senderEmail;
    private String appPassword;
    private String port = "587";
    private String security = "TLS";
}
