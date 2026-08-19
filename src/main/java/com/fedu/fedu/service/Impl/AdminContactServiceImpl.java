package com.fedu.fedu.service.Impl;

import com.fedu.fedu.dto.req.ReplyContactRequest;
import com.fedu.fedu.dto.res.ContactMessageResponse;
import com.fedu.fedu.entity.ContactMessage;
import com.fedu.fedu.repository.ContactMessageRepository;
import com.fedu.fedu.service.AdminContactService;
import com.fedu.fedu.service.MailService;
import com.fedu.fedu.utils.enums.ContactStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminContactServiceImpl implements AdminContactService {

    private final ContactMessageRepository contactMessageRepository;
    private final MailService mailService;

    @Override
    @Transactional(readOnly = true)
    public List<ContactMessageResponse> getAllContacts() {
        log.info("Fetching all contact messages for admin");
        return contactMessageRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void replyContact(Long id, ReplyContactRequest request) {
        log.info("Replying to contact message id: {}", id);
        ContactMessage contactMessage = contactMessageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact message not found"));

        contactMessage.setReplyMessage(request.getMessageResponse());
        contactMessage.setStatus(ContactStatus.REPLIED);
        contactMessageRepository.save(contactMessage);

        String emailContent = String.format(
                "<h3>Phản hồi từ FEdu</h3>" +
                "<p>Chào %s,</p>" +
                "<p>Chúng tôi đã nhận được tin nhắn của bạn với nội dung:</p>" +
                "<blockquote>%s</blockquote>" +
                "<p><b>Phản hồi:</b></p>" +
                "<p>%s</p>" +
                "<br/><p>Trân trọng,<br/>Đội ngũ FEdu</p>",
                contactMessage.getName(), contactMessage.getMessage(), request.getMessageResponse()
        );

        try {
            mailService.sendEmail(request.getSenderEmail(), contactMessage.getEmail(), "Phản hồi liên hệ từ FEdu", emailContent, null);
            log.info("Reply email sent to {}", contactMessage.getEmail());
        } catch (Exception e) {
            log.error("Failed to send reply email to {}", contactMessage.getEmail(), e);
        }
    }

    private ContactMessageResponse toResponse(ContactMessage contactMessage) {
        return ContactMessageResponse.builder()
                .id(contactMessage.getId())
                .name(contactMessage.getName())
                .email(contactMessage.getEmail())
                .subject(contactMessage.getSubject())
                .message(contactMessage.getMessage())
                .replyMessage(contactMessage.getReplyMessage())
                .status(contactMessage.getStatus())
                .createdAt(contactMessage.getCreatedAt())
                .build();
    }
}
