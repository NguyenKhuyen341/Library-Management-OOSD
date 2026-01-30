package com.library.project.service; // Dòng này phải khớp với thư mục

import com.library.project.entity.Book;
import com.library.project.entity.Loan;
import com.library.project.entity.Reader;
import com.library.project.repository.BookRepository;
import com.library.project.repository.FineRepository;
import com.library.project.repository.LoanRepository;
import com.library.project.strategy.FineCalculationStrategy;
import com.library.project.strategy.StudentFineStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private FineRepository fineRepository;
    @Mock
    private Map<String, FineCalculationStrategy> fineStrategies;

    @InjectMocks
    private LoanService loanService;

@Test
    void testReturnBook_WhenOverdue_ShouldCalculateFine() {
        // 1. GIẢ LẬP DỮ LIỆU
        Long loanId = 1L;
        
        Book book = new Book();
        book.setId(1L);
        book.setAvailableQuantity(10);

        Reader reader = new Reader();
        reader.setMembershipClass("STUDENT");

        Loan loan = new Loan();
        loan.setId(loanId);
        loan.setBook(book);
        loan.setReader(reader);
        loan.setStatus("BORROWING");
        loan.setDueDate(LocalDate.now().minusDays(14));

        // Giả lập tìm thấy phiếu mượn
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        
        // Giả lập chiến lược tính tiền
        when(fineStrategies.get("STUDENT")).thenReturn(new StudentFineStrategy());

        // --- DÒNG QUAN TRỌNG CẦN THÊM ---
        // Khi gọi hàm save, hãy trả về đối tượng loan thay vì null
        when(loanRepository.save(any())).thenReturn(loan);
        // --------------------------------

        // 2. CHẠY TEST
        Loan result = loanService.returnBook(loanId);

        // 3. KIỂM TRA
        // Kiểm tra xem repository có được gọi hàm save không
        verify(fineRepository, times(1)).save(any());
        
        // Kiểm tra kết quả (Bây giờ result sẽ không bị null nữa)
        assertEquals("RETURNED", result.getStatus());
    }
}