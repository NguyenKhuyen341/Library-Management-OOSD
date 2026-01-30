package com.library.project.controller;

import com.library.project.entity.Book;
import com.library.project.entity.Loan;
import com.library.project.entity.Reader;
import com.library.project.repository.BookRepository;
import com.library.project.repository.LoanRepository;
import com.library.project.repository.ReaderRepository;
import com.library.project.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
@CrossOrigin(origins = "*")
public class LoanController {

    @Autowired
    private LoanService loanService; // Dùng Service thay vì Repository trực tiếp cho logic nghiệp vụ

    @Autowired
    private LoanRepository loanRepository;
    @Autowired
    private ReaderRepository readerRepository;
    @Autowired
    private BookRepository bookRepository;

    @GetMapping
    public List<Loan> getAllLoans() {
        return loanService.getAllLoans();
    }

    @GetMapping("/my-loans")
    public List<Loan> getMyLoans(@RequestParam Long readerId) {
        return loanService.getLoansByReaderId(readerId);
    }

    // Đăng ký mượn (Pending) - Cái này đơn giản nên giữ ở Controller hoặc chuyển sang Service cũng được
    @PostMapping("/borrow")
    public ResponseEntity<?> borrowBook(@RequestParam Long readerId, @RequestParam Long bookId) {
        Reader reader = readerRepository.findById(readerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy độc giả!"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sách!"));

        if (book.getAvailableQuantity() <= 0) return ResponseEntity.badRequest().body("Sách đã hết hàng!");
        
        // Logic kiểm tra khác giữ nguyên...
        
        // Tạo phiếu PENDING
        Loan loan = new Loan();
        loan.setReader(reader);
        loan.setBook(book);
        loan.setBorrowDate(LocalDate.now());
        loan.setDueDate(null);
        loan.setStatus("PENDING");
        loan.setFineAmount(0.0);
        loanRepository.save(loan);

        return ResponseEntity.ok("Đăng ký thành công!");
    }

    @PostMapping("/approve/{id}")
    public ResponseEntity<?> approveLoan(@PathVariable Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu mượn"));
        if (!"PENDING".equals(loan.getStatus())) return ResponseEntity.badRequest().body("Phiếu không hợp lệ");

        loan.setStatus("BORROWING");
        loan.setBorrowDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(14));
        loanRepository.save(loan);
        return ResponseEntity.ok("Đã giao sách.");
    }

    // --- QUAN TRỌNG: API TRẢ SÁCH GỌI SERVICE ---
    @PostMapping("/return/{id}")
    public ResponseEntity<?> returnBook(@PathVariable Long id) {
        try {
            // Gọi Service để tính phạt và cập nhật kho
            Loan returnedLoan = loanService.returnBook(id);
            return ResponseEntity.ok(returnedLoan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}