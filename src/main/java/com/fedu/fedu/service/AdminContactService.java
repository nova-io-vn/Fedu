package com.fedu.fedu.service;

import com.fedu.fedu.dto.req.ReplyContactRequest;
import com.fedu.fedu.dto.res.ContactMessageResponse;

import java.util.List;

public interface AdminContactService {
    List<ContactMessageResponse> getAllContacts();
    void replyContact(Long id, ReplyContactRequest request);
}
