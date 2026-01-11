package com.library.project.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "librarians")
@PrimaryKeyJoinColumn(name = "user_id")
public class Librarian extends User {

    @Column(unique = true, nullable = false)
    private String staffCode; // Mã nhân viên

    private String position;  // Chức vụ
    private LocalDate startDate; // Ngày vào làm

    public String getStaffCode() {
        return staffCode;
    }

    public void setStaffCode(String staffCode) {
        this.staffCode = staffCode;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
}