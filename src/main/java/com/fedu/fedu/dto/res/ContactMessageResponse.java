package com.fedu.fedu.dto.res;

import com.fedu.fedu.utils.enums.ContactStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContactMessageResponse {
    Long id;
    String name;
    String email;
    String subject;
    String message;
    String replyMessage;
    ContactStatus status;
    LocalDateTime createdAt;
}
