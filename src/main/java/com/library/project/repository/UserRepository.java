package com.library.project.repository;
import com.library.project.entity.Author;
import com.library.project.entity.Book;
import com.library.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}