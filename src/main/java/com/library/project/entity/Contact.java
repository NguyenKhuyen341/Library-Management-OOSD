package com.library.project.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contacts")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String email;
    
    @Column(columnDefinition = "TEXT")
    private String message; // Nội dung khách gửi

    private LocalDateTime createdDate; // Ngày gửi
    
    private String status; // PENDING (Chờ xử lý), REPLIED (Đã phản hồi)
    
    @Column(columnDefinition = "TEXT")
    private String adminReply; // Nội dung Admin trả lời

    // Constructor
    public Contact() {
        this.createdDate = LocalDateTime.now();
        this.status = "PENDING";
    }

    // Getters và Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAdminReply() { return adminReply; }
    public void setAdminReply(String adminReply) { this.adminReply = adminReply; }
}