package com.library.project.strategy;

import org.springframework.stereotype.Component;

@Component("STUDENT") // Key nhận diện là "STUDENT"
public class StudentFineStrategy implements FineCalculationStrategy {

    @Override
    public double calculateFine(long overdueDays) {
        if (overdueDays <= 0) {
            return 0;
        }
        // Ví dụ: Sinh viên bị phạt 2.000đ mỗi ngày
        return overdueDays * 2000;
    }
}