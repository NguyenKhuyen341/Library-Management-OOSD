package com.library.project.repository;

import com.library.project.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    // Sắp xếp tin nhắn mới nhất lên đầu
    List<Contact> findAllByOrderByCreatedDateDesc();
}