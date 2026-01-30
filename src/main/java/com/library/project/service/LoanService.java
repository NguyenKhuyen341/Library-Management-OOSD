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
    // 1. Thêm Repository này để lưu tiền phạt
    private final FineRepository fineRepository;

    // 2. Map chứa các chiến lược tính phí (Spring tự tìm các file có @Component)
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

    // --- Giữ nguyên hàm mượn sách ---
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
        loan.setDueDate(LocalDate.now().plusDays(14));
        loan.setStatus("BORROWING");

        return loanRepository.save(loan);
    }

    // --- CẬP NHẬT HÀM TRẢ SÁCH (QUAN TRỌNG) ---
    @Transactional
    public Loan returnBook(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu mượn!"));

        if ("RETURNED".equals(loan.getStatus())) {
            throw new RuntimeException("Sách này đã được trả trước đó rồi!");
        }

        // --- BẮT ĐẦU LOGIC TÍNH PHẠT MỚI ---
        LocalDate returnDate = LocalDate.now(); // Ngày trả thực tế (Hôm nay)
        
        // Tính khoảng cách ngày: DueDate (Hạn) -> ReturnDate (Nay)
        long overdueDays = ChronoUnit.DAYS.between(loan.getDueDate(), returnDate);

        if (overdueDays > 0) {
            // Lấy chiến lược tính phí cho "STUDENT"
            // (Vì bạn chỉ làm cho sinh viên nên mình fix cứng luôn là STUDENT cho dễ)
            FineCalculationStrategy strategy = fineStrategies.get("STUDENT");

            if (strategy != null) {
                double amount = strategy.calculateFine(overdueDays);

                // Lưu phiếu phạt vào database
                Fine fine = new Fine();
                fine.setLoan(loan);
                fine.setFineAmount(amount);
                fine.setReason("Quá hạn " + overdueDays + " ngày");
                fine.setCreatedDate(returnDate);
                fine.setStatus("UNPAID"); // Chưa thanh toán
                
                fineRepository.save(fine);
                
                System.out.println("Đã phạt sinh viên: " + amount + " VND");
            }
        }
        // --- KẾT THÚC LOGIC PHẠT ---

        loan.setReturnDate(returnDate);
        loan.setStatus("RETURNED");

        Book book = loan.getBook();
        book.setAvailableQuantity(book.getAvailableQuantity() + 1);
        bookRepository.save(book);

        return loanRepository.save(loan);
    }

    // --- Giữ nguyên các hàm lấy danh sách ---
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    public List<Loan> getLoansByReaderId(Long readerId) {
        return loanRepository.findByReaderId(readerId);
    }
}