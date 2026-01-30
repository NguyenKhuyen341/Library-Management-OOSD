package com.library.project.service;

import com.library.project.entity.Contact;
import com.library.project.repository.ContactRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactService {

    private final ContactRepository contactRepository;

    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    // Lấy tất cả liên hệ
    public List<Contact> getAllContacts() {
        return contactRepository.findAllByOrderByCreatedDateDesc();
    }

    // Lưu liên hệ mới (từ khách)
    public Contact createContact(Contact contact) {
        contact.setStatus("PENDING"); // Mặc định là chờ xử lý
        return contactRepository.save(contact);
    }

    // Admin trả lời
    public Contact replyContact(Long id, String replyMessage) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy liên hệ!"));
        
        contact.setAdminReply(replyMessage);
        contact.setStatus("REPLIED"); // Đổi trạng thái thành Đã phản hồi
        
        return contactRepository.save(contact);
    }
    
    // Xóa liên hệ
    public void deleteContact(Long id) {
        contactRepository.deleteById(id);
    }
}