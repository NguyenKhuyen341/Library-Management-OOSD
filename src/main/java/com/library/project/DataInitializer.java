package com.library.project;

import com.library.project.entity.*;
import com.library.project.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ReaderRepository readerRepository;
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    public DataInitializer(ReaderRepository readerRepository,
                        BookRepository bookRepository,
                        AuthorRepository authorRepository) {
        this.readerRepository = readerRepository;
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Chỉ nạp dữ liệu nếu kho sách đang trống
        if (bookRepository.count() == 0) {
            System.out.println("---- BẮT ĐẦU NẠP DỮ LIỆU MẪU ----");

            // 1. Tạo Độc giả mẫu (Admin & User)
            createReader("admin", "123", "Quản Trị Viên", "ADMIN01");
            createReader("reader01", "123", "Nguyễn Văn A", "SV001");
            createReader("reader02", "123", "Trần Thị B", "SV002");

            // 2. Nạp nhanh danh sách sách (Tiêu đề, Tác giả, Thể loại, Giá, Số lượng, Ảnh)
            // Lưu ý: Ảnh bạn phải tự copy file vào thư mục 'uploads' hoặc dùng link ảnh online nếu sửa code

            addBook("Nhà Giả Kim", "Paulo Coelho", "VANHOC", 79000, 20, "nha-gia-kim.jpg");
            addBook("Đắc Nhân Tâm", "Dale Carnegie", "KYNANG", 85000, 50, "dac-nhan-tam.jpg");
            addBook("Mắt Biếc", "Nguyễn Nhật Ánh", "VANHOC", 110000, 15, "mat-biec.jpg"); // Ảnh bạn đã upload
            addBook("Tôi Thấy Hoa Vàng Trên Cỏ Xanh", "Nguyễn Nhật Ánh", "VANHOC", 95000, 20, "hoa-vang.jpg");
            addBook("Clean Code", "Robert C. Martin", "CNTT", 450000, 10, "clean-code.jpg");
            addBook("Introduction to Algorithms", "Thomas H. Cormen", "CNTT", 600000, 5, "algorithms.jpg");
            addBook("Dạy Con Làm Giàu", "Robert Kiyosaki", "KINHTE", 120000, 30, "day-con-lam-giau.jpg");
            addBook("Tiếng Anh Cho Người Mất Gốc", "Trang Anh", "NGOAINGU", 150000, 40, "tieng-anh.jpg");
            addBook("Vật Lý Đại Cương A1", "Lương Duyên Bình", "KHOAHOC", 55000, 100, "vat-ly.jpg");
            addBook("Giáo Trình Triết Học Mác-Lênin", "Bộ Giáo Dục", "KHOAHOC", 40000, 200, "triet-hoc.jpg");

            System.out.println("---- ĐÃ NẠP XONG DỮ LIỆU ----");
        }
    }

    // --- HÀM HỖ TRỢ THÊM SÁCH NHANH ---
    private void addBook(String title, String authorName, String category, double price, int qty, String img) {
        // 1. Tìm tác giả, nếu chưa có thì tạo mới
        Author author = authorRepository.findByFullName(authorName)
                .orElseGet(() -> {
                    Author newAuthor = new Author();
                    newAuthor.setFullName(authorName);
                    return authorRepository.save(newAuthor);
                });

        // 2. Tạo sách
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setCategory(category); // Hiện tại code của bạn Category đang là String
        book.setPrice(price);
        book.setTotalQuantity(qty);
        book.setAvailableQuantity(qty);
        book.setImage(img); // Lưu tên file ảnh
        book.setDescription("Mô tả mặc định cho cuốn sách " + title);
        book.setPublishYear(2023);
        book.setPageCount(300);
        book.setLanguage("Tiếng Việt");

        bookRepository.save(book);
    }

    // --- HÀM HỖ TRỢ TẠO USER ---
    private void createReader(String username, String pass, String fullname, String code) {
        if (readerRepository.findByUsername(username) == null) {
            Reader r = new Reader();
            r.setUsername(username);
            r.setPassword(pass);
            r.setFullName(fullname);
            r.setReaderCode(code);
            readerRepository.save(r);
            r.setMembershipClass("STUDENT");
            
            readerRepository.save(r);
        }
    }
}