package com.library.project;

import com.library.project.entity.*;
import com.library.project.repository.*;
import com.library.project.service.LoanService; // Nhớ dòng import này
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ReaderRepository readerRepository;
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final LoanService loanService; // Đây chính là biến bị thiếu

    public DataInitializer(ReaderRepository readerRepository,
                           BookRepository bookRepository,
                           AuthorRepository authorRepository,
                           LoanService loanService) {
        this.readerRepository = readerRepository;
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.loanService = loanService;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Thêm Tác giả mẫu
        Author author = new Author();
        author.setFullName("Nguyễn Nhật Ánh");
        author.setPenName("AnhNN");
        authorRepository.save(author);

        // 2. Thêm Độc giả mẫu
        Reader reader = new Reader();
        reader.setUsername("reader01");
        reader.setPassword("123456");
        reader.setFullName("Nguyễn Văn A");
        reader.setReaderCode("DG001");
        readerRepository.save(reader);

        // 3. Thêm Sách mẫu
        Book book = new Book();
        book.setTitle("Cho tôi xin một vé đi tuổi thơ");
        book.setAuthor(author);
        book.setTotalQuantity(10);
        book.setAvailableQuantity(10); // Ban đầu còn 10 cuốn
        bookRepository.save(book);

        System.out.println("---- ĐÃ KHỞI TẠO DỮ LIỆU MẪU THÀNH CÔNG ----");

        // 4. Test Mượn sách
        try {
            // Mượn cuốn sách vừa tạo cho độc giả vừa tạo
            loanService.createLoan(reader, book);
            System.out.println("---- ĐÃ TẠO PHIẾU MƯỢN MẪU THÀNH CÔNG ----");
        } catch (Exception e) {
            System.out.println("Lỗi mượn sách: " + e.getMessage());
        }
    }
}