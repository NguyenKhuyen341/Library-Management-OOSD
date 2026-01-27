package com.library.project.controller;

import com.library.project.entity.Author;
import com.library.project.entity.Book;
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

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class BookController {

    private final BookService bookService;
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final LoanRepository loanRepository;

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
    public List<Book> getAllBooks(@RequestParam(required = false) String keyword, @RequestParam(required = false) String category) {
        if (category != null && !category.isEmpty()) return bookRepository.findByCategory(category);
        if (keyword != null && !keyword.isEmpty()) return bookService.searchBooks(keyword);
        return bookService.getAllBooks();
    }

    @GetMapping("/{id}")
    public Book getBookById(@PathVariable Long id) {
        return bookService.getBookById(id);
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public Book addBook(
            @RequestParam("title") String title,
            @RequestParam("authorName") String authorName,
            @RequestParam("price") double price,
            @RequestParam("quantity") int quantity,
            @RequestParam("category") String category,
            @RequestParam("description") String description,
            @RequestParam("publishYear") Integer publishYear,
            @RequestParam("pageCount") Integer pageCount,
            @RequestParam("language") String language,
            @RequestParam(value = "image", required = false) MultipartFile imageFile
    ) throws IOException {
        Book book = new Book();
        book.setTitle(title);
        book.setPrice(price);
        book.setTotalQuantity(quantity);
        book.setAvailableQuantity(quantity);
        book.setCategory(category);
        book.setDescription(description);
        book.setPublishYear(publishYear);
        book.setPageCount(pageCount);
        book.setLanguage(language);

        if (authorName != null && !authorName.isEmpty()) {
            Author author = authorRepository.findByFullName(authorName).orElseGet(() -> {
                Author newAuthor = new Author();
                newAuthor.setFullName(authorName);
                return authorRepository.save(newAuthor);
            });
            book.setAuthor(author);
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            book.setImage(saveImage(imageFile));
        }
        return bookRepository.save(book);
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateBook(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("authorName") String authorName,
            @RequestParam("price") double price,
            @RequestParam("quantity") int quantity,
            @RequestParam("category") String category,
            @RequestParam("description") String description,
            @RequestParam("publishYear") Integer publishYear,
            @RequestParam("pageCount") Integer pageCount,
            @RequestParam("language") String language,
            @RequestParam(value = "image", required = false) MultipartFile imageFile
    ) throws IOException {
        Book book = bookRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy sách!"));

        int currentlyBorrowed = book.getTotalQuantity() - book.getAvailableQuantity();
        if (quantity < currentlyBorrowed) {
            return ResponseEntity.badRequest().body("Không thể giảm số lượng xuống vì đang có " + currentlyBorrowed + " cuốn được mượn!");
        }

        book.setAvailableQuantity(book.getAvailableQuantity() + (quantity - book.getTotalQuantity()));
        book.setTotalQuantity(quantity);
        book.setTitle(title);
        book.setPrice(price);
        book.setCategory(category);
        book.setDescription(description);
        book.setPublishYear(publishYear);
        book.setPageCount(pageCount);
        book.setLanguage(language);

        if (authorName != null && !authorName.isEmpty()) {
            Author author = authorRepository.findByFullName(authorName).orElseGet(() -> {
                Author newAuthor = new Author();
                newAuthor.setFullName(authorName);
                return authorRepository.save(newAuthor);
            });
            book.setAuthor(author);
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            book.setImage(saveImage(imageFile));
        }
        return ResponseEntity.ok(bookRepository.save(book));
    }

    private String saveImage(MultipartFile file) throws IOException {
        String uploadDir = "./uploads";
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        }
        return fileName;
    }
}