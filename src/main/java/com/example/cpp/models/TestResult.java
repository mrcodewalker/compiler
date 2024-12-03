package com.example.cpp.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TestResult {
    public boolean passed;
    public String message;
    public long startTime;
    public long endTime;
    public long executionTime;
    public boolean timeLimit;
    public Long memoryUsed;
}
