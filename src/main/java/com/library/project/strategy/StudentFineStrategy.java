package com.library.project.strategy;

import org.springframework.stereotype.Component;

@Component("STUDENT")
public class StudentFineStrategy implements FineCalculationStrategy {

    @Override
    public double calculateFine(long overdueDays) {
        if (overdueDays <= 0) {
            return 0;
        }
        // Phạt 5.000 VNĐ mỗi ngày (Ví dụ cập nhật giá)
        return overdueDays * 5000;
    }
}