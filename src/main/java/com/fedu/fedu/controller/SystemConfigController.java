package com.fedu.fedu.controller;

import com.fedu.fedu.dto.req.EmailConfigDTO;
import com.fedu.fedu.dto.res.ResponseData;
import com.fedu.fedu.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/config")
@RequiredArgsConstructor
@Tag(name = "System Configuration", description = "Endpoints for admin to manage system configuration")
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @Operation(summary = "Save Email Configuration")
    @PostMapping("/email")
    public ResponseData<Void> saveEmailConfig(@RequestBody EmailConfigDTO configDTO) {
        systemConfigService.saveEmailConfig(configDTO);
        return new ResponseData<>(HttpStatus.OK.value(), "Cấu hình email đã được cập nhật thành công.");
    }

    @Operation(summary = "Get Email Configuration")
    @GetMapping("/email")
    public ResponseData<EmailConfigDTO> getEmailConfig() {
        return new ResponseData<>(HttpStatus.OK.value(), "Lấy cấu hình email thành công.", systemConfigService.getEmailConfig());
    }

    @Operation(summary = "Test Email Configuration")
    @PostMapping("/email/test")
    public ResponseData<Void> testEmailConfig(@RequestBody EmailConfigDTO configDTO) {
        try {
            systemConfigService.testEmailConfig(configDTO);
            return new ResponseData<>(HttpStatus.OK.value(), "Kiểm tra cấu hình kết nối thành công.");
        } catch (Exception e) {
            return new ResponseData<>(HttpStatus.BAD_REQUEST.value(), "Kết nối thất bại: " + e.getMessage());
        }
    }
}
