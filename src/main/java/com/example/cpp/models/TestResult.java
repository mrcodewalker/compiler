package com.example.cpp.models;

public class TestResult {
    public boolean passed;
    public String message;
    public long executionTime;
    public long finishTime;
    public boolean timeLimit;

    public TestResult(boolean passed, String message, long executionTime, boolean timeLimit) {
        this.passed = passed;
        this.message = message;
        this.executionTime = executionTime;
        this.timeLimit = timeLimit;
    }
}
