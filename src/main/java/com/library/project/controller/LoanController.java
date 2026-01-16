package com.library.project.controller;

import com.library.project.entity.Book;
import com.library.project.entity.Loan;
import com.library.project.entity.Reader;
import com.library.project.repository.BookRepository;
import com.library.project.repository.LoanRepository;
import com.library.project.repository.ReaderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
@CrossOrigin(origins = "*") // Quan trọng: Để tránh lỗi chặn kết nối
public class LoanController {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReaderRepository readerRepository;

    // 1. API: Admin lấy TOÀN BỘ danh sách (Dùng cho trang Admin)
    @GetMapping
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    // 2. API: Sinh viên lấy danh sách CỦA MÌNH (Dùng cho trang Student Dashboard)
    @GetMapping("/my-loans")
    public List<Loan> getMyLoans(@RequestParam Long readerId) {
        return loanRepository.findByReaderId(readerId);
    }

    // 3. API: Đăng ký mượn sách (Sinh viên bấm -> PENDING)
    @PostMapping("/borrow")
    public ResponseEntity<?> borrowBook(
            @RequestParam Long readerId,
            @RequestParam Long bookId
    ) {
        Reader reader = readerRepository.findById(readerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy độc giả!"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sách!"));

        // --- Kiểm tra luật ---
        if (book.getAvailableQuantity() <= 0) {
            return ResponseEntity.badRequest().body("Sách này đã hết hàng!");
        }
        long currentLoans = loanRepository.countBorrowingBooks(readerId);
        if (currentLoans >= 3) {
            return ResponseEntity.badRequest().body("Bạn đang mượn quá 3 cuốn!");
        }
        boolean isDuplicate = loanRepository.isBookAlreadyBorrowed(readerId, bookId);
        if (isDuplicate) {
            return ResponseEntity.badRequest().body("Bạn đang giữ cuốn sách này rồi!");
        }

        // --- Xử lý ---
        // Trừ kho tạm thời
        book.setAvailableQuantity(book.getAvailableQuantity() - 1);
        bookRepository.save(book);

        // Tạo phiếu PENDING
        Loan loan = new Loan();
        loan.setReader(reader);
        loan.setBook(book);
        loan.setBorrowDate(LocalDate.now());
        loan.setDueDate(null); // Chưa tính hạn trả
        loan.setStatus("PENDING");

        loanRepository.save(loan);

        return ResponseEntity.ok("Đăng ký thành công! Vui lòng đến thư viện nhận sách.");
    }

    // 4. API: Admin DUYỆT giao sách (PENDING -> BORROWING)
    @PostMapping("/approve/{id}")
    public ResponseEntity<?> approveLoan(@PathVariable Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu mượn"));

        if (!"PENDING".equals(loan.getStatus())) {
            return ResponseEntity.badRequest().body("Phiếu này không ở trạng thái chờ!");
        }

        loan.setStatus("BORROWING");
        loan.setBorrowDate(LocalDate.now()); // Bắt đầu tính ngày
        loan.setDueDate(LocalDate.now().plusDays(14)); // Hạn 14 ngày

        loanRepository.save(loan);
        return ResponseEntity.ok("Đã giao sách.");
    }

    // 5. API: Admin TRẢ SÁCH (BORROWING -> RETURNED)
    @PostMapping("/return/{id}")
    public ResponseEntity<?> returnBook(@PathVariable Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu mượn"));

        if ("RETURNED".equals(loan.getStatus())) {
            return ResponseEntity.badRequest().body("Sách này đã được trả rồi!");
        }

        // Đổi trạng thái
        loan.setStatus("RETURNED");
        loan.setDueDate(LocalDate.now()); // Ngày trả thực tế

        // Cộng lại kho
        Book book = loan.getBook();
        book.setAvailableQuantity(book.getAvailableQuantity() + 1);
        bookRepository.save(book);

        loanRepository.save(loan);
        return ResponseEntity.ok("Đã trả sách và cộng lại kho.");
    }
}