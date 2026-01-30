package com.library.project.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate borrowDate; // Ngày mượn
    private LocalDate dueDate;    // Hạn trả
    private LocalDate returnDate; // Ngày trả thực tế
    private String status;        // Trạng thái (BORROWING, RETURNED, OVERDUE)
    private String conditionNote; // Tình trạng

    // --- MỚI: Lưu tiền phạt trực tiếp vào Loan để dễ hiển thị ---
    @Column(name = "fine_amount")
    private Double fineAmount = 0.0;

    // --- Quan hệ ---
    @ManyToOne
    @JoinColumn(name = "student_id")
    private Reader reader;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    // --- Getter & Setter ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getConditionNote() { return conditionNote; }
    public void setConditionNote(String conditionNote) { this.conditionNote = conditionNote; }

    public Double getFineAmount() { return fineAmount; }
    public void setFineAmount(Double fineAmount) { this.fineAmount = fineAmount; }

    public Reader getReader() { return reader; }
    public void setReader(Reader reader) { this.reader = reader; }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
}