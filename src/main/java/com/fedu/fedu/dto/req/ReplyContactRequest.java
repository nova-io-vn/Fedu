package com.fedu.fedu.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReplyContactRequest {
    @NotBlank(message = "Message response cannot be blank")
    String messageResponse;
}
