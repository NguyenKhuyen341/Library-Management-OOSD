package com.library.project;

import com.library.project.entity.*;
import com.library.project.repository.*;
import com.library.project.service.LoanService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ReaderRepository readerRepository;
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final LoanService loanService;

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
        // KIỂM TRA: Nếu kho sách chưa có gì (count == 0) thì mới tạo
        if (bookRepository.count() == 0) {

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
            book.setAvailableQuantity(10);
            bookRepository.save(book);

            System.out.println("---- ĐÃ KHỞI TẠO DỮ LIỆU MẪU THÀNH CÔNG ----");

            // 4. Test Mượn sách (ĐỂ TRONG NÀY MỚI DÙNG ĐƯỢC BIẾN reader VÀ book)
            try {
                loanService.createLoan(reader, book);
                System.out.println("---- ĐÃ TẠO PHIẾU MƯỢN MẪU THÀNH CÔNG ----");
            } catch (Exception e) {
                System.out.println("Lỗi mượn sách: " + e.getMessage());
            }

        } else {
            // Nếu dữ liệu có rồi thì thôi, không làm gì cả
            System.out.println("---- DỮ LIỆU ĐÃ CÓ SẴN (SKIP DATA SEEDING) ----");
        }
    }
}