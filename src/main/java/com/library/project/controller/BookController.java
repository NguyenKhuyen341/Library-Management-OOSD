package com.library.project.controller;

import com.library.project.entity.Author;
import com.library.project.entity.Book;
import com.library.project.entity.Loan;
import com.library.project.repository.AuthorRepository;
import com.library.project.repository.BookRepository;
import com.library.project.repository.LoanRepository;
import com.library.project.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*") // Cho phép Frontend gọi API thoải mái
public class BookController {

    private final BookService bookService;
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final LoanRepository loanRepository; // Thêm cái này để kiểm tra mượn trả

    // Inject LoanRepository vào Constructor
    public BookController(BookService bookService,
                          BookRepository bookRepository,
                          AuthorRepository authorRepository,
                          LoanRepository loanRepository) {
        this.bookService = bookService;
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.loanRepository = loanRepository;
    }

    @GetMapping
    public List<Book> getAllBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category
    ) {
        if (category != null && !category.isEmpty()) {
            return bookRepository.findByCategory(category);
        }
        if (keyword != null && !keyword.isEmpty()) {
            return bookService.searchBooks(keyword);
        }
        return bookService.getAllBooks();
    }

    @GetMapping("/{id}")
    public Book getBookById(@PathVariable Long id) {
        return bookService.getBookById(id);
    }

    // --- HÀM THÊM SÁCH ---
    @PostMapping(consumes = {"multipart/form-data"})
    public Book addBook(
            @RequestParam("title") String title,
            @RequestParam("authorName") String authorName,
            @RequestParam("price") double price,
            @RequestParam("quantity") int quantity,
            @RequestParam("category") String category,
            @RequestParam("description") String description,
            @RequestParam(value = "image", required = false) MultipartFile imageFile
    ) throws IOException {

        Book book = new Book();
        book.setTitle(title);
        book.setPrice(price);
        book.setTotalQuantity(quantity);
        book.setAvailableQuantity(quantity);
        book.setCategory(category);
        book.setDescription(description);

        // Xử lý Tác giả
        if (authorName != null && !authorName.isEmpty()) {
            Author author = authorRepository.findByFullName(authorName)
                    .orElseGet(() -> {
                        Author newAuthor = new Author();
                        newAuthor.setFullName(authorName);
                        return authorRepository.save(newAuthor);
                    });
            book.setAuthor(author);
        }

        // Xử lý Lưu Ảnh
        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = saveImage(imageFile);
            book.setImage(fileName);
        }

        return bookRepository.save(book);
    }

    // --- HÀM CẬP NHẬT SÁCH (SỬA LẠI ĐỂ BÁO LỖI NẾU SỐ LƯỢNG KHÔNG HỢP LỆ) ---
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateBook(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("authorName") String authorName,
            @RequestParam("price") double price,
            @RequestParam("quantity") int quantity, // Số lượng tổng mới
            @RequestParam("category") String category,
            @RequestParam("description") String description,
            @RequestParam(value = "image", required = false) MultipartFile imageFile
    ) throws IOException {

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sách!"));

        // --- 1. LOGIC KIỂM TRA SỐ LƯỢNG AN TOÀN ---
        int oldTotal = book.getTotalQuantity();
        int oldAvailable = book.getAvailableQuantity();
        int currentlyBorrowed = oldTotal - oldAvailable; // Số sách đang bị mượn

        // Nếu Số lượng mới < Số sách đang bị mượn -> BÁO LỖI NGAY
        if (quantity < currentlyBorrowed) {
            return ResponseEntity.badRequest().body("Không thể giảm số lượng xuống " + quantity +
                    " vì đang có " + currentlyBorrowed + " cuốn được mượn!");
        }

        // Nếu hợp lệ thì tính toán lại kho
        int difference = quantity - oldTotal;
        book.setTotalQuantity(quantity);
        book.setAvailableQuantity(oldAvailable + difference);
        // ----------------------------------------

        book.setTitle(title);
        book.setPrice(price);
        book.setCategory(category);
        book.setDescription(description);

        if (authorName != null && !authorName.isEmpty()) {
            Author author = authorRepository.findByFullName(authorName)
                    .orElseGet(() -> {
                        Author newAuthor = new Author();
                        newAuthor.setFullName(authorName);
                        return authorRepository.save(newAuthor);
                    });
            book.setAuthor(author);
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = saveImage(imageFile);
            book.setImage(fileName);
        }

        Book savedBook = bookRepository.save(book);
        return ResponseEntity.ok(savedBook);
    }

    // --- HÀM XÓA SÁCH (SỬA LẠI ĐỂ XỬ LÝ LỊCH SỬ MƯỢN TRẢ) ---
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sách!"));

        // Lấy tất cả phiếu mượn liên quan đến sách này
        List<Loan> allLoans = loanRepository.findAll();

        // 1. Kiểm tra xem có ai ĐANG mượn không? (Trạng thái BORROWING)
        boolean isCurrentlyBorrowed = allLoans.stream()
                .anyMatch(loan -> loan.getBook().getId().equals(id) && "BORROWING".equals(loan.getStatus()));

        if (isCurrentlyBorrowed) {
            return ResponseEntity.badRequest().body("Không thể xóa! Sách này đang có người mượn chưa trả.");
        }

        // 2. Nếu không ai đang mượn -> Xóa sạch lịch sử đã trả (RETURNED) của sách này
        List<Loan> historyLoans = allLoans.stream()
                .filter(loan -> loan.getBook().getId().equals(id))
                .collect(Collectors.toList());

        if (!historyLoans.isEmpty()) {
            loanRepository.deleteAll(historyLoans);
        }

        // 3. Cuối cùng mới xóa sách
        bookRepository.delete(book);

        return ResponseEntity.ok().body("Đã xóa sách và toàn bộ lịch sử liên quan.");
    }

    // --- HÀM PHỤ: LƯU FILE ---
    private String saveImage(MultipartFile file) throws IOException {
        String uploadDir = "./uploads";
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        try (InputStream inputStream = file.getInputStream()) {
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        return fileName;
    }
}