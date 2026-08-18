package com.fedu.fedu.entity;

import com.fedu.fedu.utils.enums.ContactStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "contact_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContactMessage extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    String email;

    @Column(nullable = false)
    String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    String message;

    @Column(name = "reply_message", columnDefinition = "TEXT")
    String replyMessage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    ContactStatus status;
}
