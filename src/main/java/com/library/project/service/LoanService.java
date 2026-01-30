package com.library.project.service;

import com.library.project.entity.Book;
import com.library.project.entity.Fine;
import com.library.project.entity.Loan;
import com.library.project.entity.Reader;
import com.library.project.repository.BookRepository;
import com.library.project.repository.FineRepository;
import com.library.project.repository.LoanRepository;
import com.library.project.strategy.FineCalculationStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final FineRepository fineRepository;
    private final Map<String, FineCalculationStrategy> fineStrategies;

    public LoanService(LoanRepository loanRepository,
                    BookRepository bookRepository,
                    FineRepository fineRepository,
                    Map<String, FineCalculationStrategy> fineStrategies) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.fineRepository = fineRepository;
        this.fineStrategies = fineStrategies;
    }

    @Transactional
    public Loan createLoan(Reader reader, Book book) {
        if (book.getAvailableQuantity() <= 0) {
            throw new RuntimeException("Sách đã hết trong kho!");
        }
        book.setAvailableQuantity(book.getAvailableQuantity() - 1);
        bookRepository.save(book);

        Loan loan = new Loan();
        loan.setReader(reader);
        loan.setBook(book);
        loan.setBorrowDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(14)); // Mượn 14 ngày
        loan.setStatus("BORROWING");
        loan.setFineAmount(0.0); // Mặc định chưa có phạt

        return loanRepository.save(loan);
    }

    // --- HÀM TRẢ SÁCH ĐÃ SỬA LOGIC ---
    @Transactional
    public Loan returnBook(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu mượn!"));

        if ("RETURNED".equals(loan.getStatus())) {
            throw new RuntimeException("Sách này đã được trả trước đó rồi!");
        }

        LocalDate returnDate = LocalDate.now(); // Ngày trả là HÔM NAY
        
        // 1. Tính toán quá hạn
        long overdueDays = ChronoUnit.DAYS.between(loan.getDueDate(), returnDate);
        double fineAmount = 0.0;

        if (overdueDays > 0) {
            // Sử dụng Strategy Pattern để tính tiền
            FineCalculationStrategy strategy = fineStrategies.get("STUDENT");
            if (strategy != null) {
                fineAmount = strategy.calculateFine(overdueDays);
                
                // Lưu vào bảng Fine (Lịch sử phạt chi tiết)
                Fine fine = new Fine();
                fine.setLoan(loan);
                fine.setFineAmount(fineAmount);
                fine.setReason("Quá hạn " + overdueDays + " ngày");
                fine.setCreatedDate(returnDate);
                fine.setStatus("UNPAID");
                fineRepository.save(fine);
            }
        }

        // 2. Cập nhật thông tin phiếu mượn
        loan.setReturnDate(returnDate); // Quan trọng: Set đúng ngày trả thực tế
        loan.setStatus("RETURNED");
        loan.setFineAmount(fineAmount); // Cập nhật tiền phạt vào đây để hiện lên bảng

        // 3. Cộng lại kho sách
        Book book = loan.getBook();
        book.setAvailableQuantity(book.getAvailableQuantity() + 1);
        bookRepository.save(book);

        return loanRepository.save(loan);
    }

    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    public List<Loan> getLoansByReaderId(Long readerId) {
        return loanRepository.findByReaderId(readerId);
    }
}