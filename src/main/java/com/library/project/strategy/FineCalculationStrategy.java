package com.library.project.strategy;

public interface FineCalculationStrategy {
    // Input: Số ngày quá hạn
    // Output: Số tiền phạt (VNĐ)
    double calculateFine(long overdueDays);
}