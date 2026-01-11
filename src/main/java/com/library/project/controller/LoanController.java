package com.library.project.controller;

import com.library.project.entity.Book;
import com.library.project.entity.Loan;
import com.library.project.entity.Reader;
import com.library.project.repository.BookRepository;
import com.library.project.repository.ReaderRepository;
import com.library.project.service.LoanService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;
    private final ReaderRepository readerRepository;
    private final BookRepository bookRepository;

    public LoanController(LoanService loanService, ReaderRepository readerRepository, BookRepository bookRepository) {
        this.loanService = loanService;
        this.readerRepository = readerRepository;
        this.bookRepository = bookRepository;
    }

    // API Mượn sách
    // Cách dùng (POST): /api/loans/borrow?readerId=1&bookId=1
    @PostMapping("/borrow")
    public String borrowBook(@RequestParam Long readerId, @RequestParam Long bookId) {
        // 1. Tìm độc giả
        Reader reader = readerRepository.findById(readerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy độc giả!"));

        // 2. Tìm sách
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sách!"));

        // 3. Gọi Service để mượn
        try {
            Loan newLoan = loanService.createLoan(reader, book);
            return "Mượn thành công! Mã phiếu: " + newLoan.getId();
        } catch (Exception e) {
            return "Lỗi: " + e.getMessage();
        }
    }

    // API Trả sách
    // Cách dùng (POST): /api/loans/return/1
    @PostMapping("/return/{loanId}")
    public String returnBook(@PathVariable Long loanId) {
        try {
            loanService.returnBook(loanId);
            return "Trả sách thành công!";
        } catch (Exception e) {
            return "Lỗi: " + e.getMessage();
        }
    }
}