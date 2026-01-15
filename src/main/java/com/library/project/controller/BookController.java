package com.library.project.controller;

import com.library.project.entity.Author;
import com.library.project.entity.Book;
import com.library.project.repository.AuthorRepository;
import com.library.project.repository.BookRepository;
import com.library.project.service.BookService;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    public BookController(BookService bookService, BookRepository bookRepository, AuthorRepository authorRepository) {
        this.bookService = bookService;
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
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

    // --- HÀM THÊM SÁCH (TỰ TẠO TÁC GIẢ NẾU CHƯA CÓ) ---
    @PostMapping(consumes = {"multipart/form-data"})
    public Book addBook(
            @RequestParam("title") String title,
            @RequestParam("authorName") String authorName,
            @RequestParam("price") double price,
            @RequestParam("quantity") int quantity,
            @RequestParam("category") String category,
            @RequestParam("description") String description,
            @RequestParam(value = "image", required = false) MultipartFile imageFile // Nhận file ảnh
    ) throws IOException {

        Book book = new Book();
        book.setTitle(title);
        book.setPrice(price);
        book.setTotalQuantity(quantity);
        book.setAvailableQuantity(quantity);
        book.setCategory(category);
        book.setDescription(description);

        // 1. Xử lý Tác giả (Logic cũ)
        if (authorName != null && !authorName.isEmpty()) {
            Author author = authorRepository.findByFullName(authorName)
                    .orElseGet(() -> {
                        Author newAuthor = new Author();
                        newAuthor.setFullName(authorName);
                        return authorRepository.save(newAuthor);
                    });
            book.setAuthor(author);
        }

        // 2. Xử lý Lưu Ảnh
        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = saveImage(imageFile); // Gọi hàm lưu ảnh bên dưới
            book.setImage(fileName);
        }

        return bookRepository.save(book);
    }

    // --- HÀM CẬP NHẬT SÁCH (CÓ UPLOAD ẢNH) ---
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public Book updateBook(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("authorName") String authorName,
            @RequestParam("price") double price,
            @RequestParam("quantity") int quantity,
            @RequestParam("category") String category,
            @RequestParam("description") String description,
            @RequestParam(value = "image", required = false) MultipartFile imageFile
    ) throws IOException {

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sách!"));

        book.setTitle(title);
        book.setPrice(price);
        book.setTotalQuantity(quantity);
        book.setCategory(category);
        book.setDescription(description);

        // Cập nhật tác giả
        if (authorName != null && !authorName.isEmpty()) {
            Author author = authorRepository.findByFullName(authorName)
                    .orElseGet(() -> {
                        Author newAuthor = new Author();
                        newAuthor.setFullName(authorName);
                        return authorRepository.save(newAuthor);
                    });
            book.setAuthor(author);
        }

        // Cập nhật ảnh (Chỉ lưu nếu người dùng chọn ảnh mới)
        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = saveImage(imageFile);
            book.setImage(fileName);
        }

        return bookRepository.save(book);
    }

    // --- HÀM PHỤ: LƯU FILE VÀO THƯ MỤC "uploads" ---
    private String saveImage(MultipartFile file) throws IOException {
        String uploadDir = "./uploads";
        Path uploadPath = Paths.get(uploadDir);

        // Tạo thư mục nếu chưa có
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Tạo tên file độc nhất (để không bị trùng)
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        // Lưu file
        try (InputStream inputStream = file.getInputStream()) {
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        return fileName; // Trả về tên file để lưu vào DB
    }

    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
    }
}