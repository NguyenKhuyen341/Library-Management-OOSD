package com.library.project.repository;

import com.library.project.entity.Reader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReaderRepository extends JpaRepository<Reader, Long> {
    // JpaRepository đã có sẵn các hàm save(), findAll(), deleteById()...
}