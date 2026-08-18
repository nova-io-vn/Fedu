package com.fedu.fedu.controller;

import com.fedu.fedu.dto.req.ReplyContactRequest;
import com.fedu.fedu.dto.res.ContactMessageResponse;
import com.fedu.fedu.dto.res.ResponseData;
import com.fedu.fedu.service.AdminContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/contacts")
@RequiredArgsConstructor
@Tag(name = "Admin Contact Management", description = "Endpoints for admin to manage contact messages")
public class AdminContactController {

    private final AdminContactService adminContactService;

    @Operation(summary = "Get all contact messages")
    @GetMapping
    public ResponseData<List<ContactMessageResponse>> getAllContacts() {
        return new ResponseData<>(HttpStatus.OK.value(), "Lấy danh sách liên hệ thành công", adminContactService.getAllContacts());
    }

    @Operation(summary = "Reply to a contact message")
    @PostMapping("/{id}/reply")
    public ResponseData<Void> replyContact(@PathVariable Long id, @Valid @RequestBody ReplyContactRequest request) {
        adminContactService.replyContact(id, request);
        return new ResponseData<>(HttpStatus.OK.value(), "Đã trả lời liên hệ");
    }
}
