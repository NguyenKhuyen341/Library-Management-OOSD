package com.library.project.controller;

import com.library.project.entity.Contact;
import com.library.project.service.ContactService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contacts")
@CrossOrigin(origins = "*")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    // 1. API cho Admin lấy danh sách
    @GetMapping
    public List<Contact> getAllContacts() {
        return contactService.getAllContacts();
    }

    // 2. API cho Khách gửi tin nhắn (Public)
    @PostMapping
    public ResponseEntity<?> sendContact(@RequestBody Contact contact) {
        try {
            Contact savedContact = contactService.createContact(contact);
            return ResponseEntity.ok(savedContact);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi gửi liên hệ: " + e.getMessage());
        }
    }

    // 3. API cho Admin trả lời
    @PutMapping("/{id}/reply")
    public ResponseEntity<?> replyContact(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String replyMessage = payload.get("reply");
        Contact contact = contactService.replyContact(id, replyMessage);
        return ResponseEntity.ok(contact);
    }
    
    // 4. API Xóa
    @DeleteMapping("/{id}")
    public void deleteContact(@PathVariable Long id) {
        contactService.deleteContact(id);
    }
}