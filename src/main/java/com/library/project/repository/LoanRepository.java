package com.library.project.repository;

import com.library.project.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    // Tìm danh sách mượn theo ID độc giả
    // (Spring Data JPA tự hiểu readerId là tìm theo field id của Reader)
    List<Loan> findByReaderId(Long readerId);

    // 1. Đếm số sách ĐANG MƯỢN
    // SỬA: l.reader.readerId -> l.reader.id
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.reader.id = :readerId AND l.status = 'BORROWING'")
    long countBorrowingBooks(@Param("readerId") Long readerId);

    // 2. Kiểm tra mượn trùng
    // SỬA: l.reader.readerId -> l.reader.id
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Loan l WHERE l.reader.id = :readerId AND l.book.id = :bookId AND l.status = 'BORROWING'")
    boolean isBookAlreadyBorrowed(@Param("readerId") Long readerId, @Param("bookId") Long bookId);
}