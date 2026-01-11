package com.library.project.controller;

import com.library.project.entity.Book;
import com.library.project.service.BookService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController // Đánh dấu đây là nơi tiếp nhận API
@RequestMapping("/api/books") // Địa chỉ truy cập chung: http://localhost:8080/api/books
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // 1. API Lấy danh sách tất cả sách
    // Cách dùng: Vào trình duyệt gõ http://localhost:8080/api/books
    @GetMapping
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    // 2. API Xem chi tiết 1 cuốn sách
    // Cách dùng: http://localhost:8080/api/books/1
    @GetMapping("/{id}")
    public Book getBookById(@PathVariable Long id) {
        return bookService.getBookById(id);
    }
}