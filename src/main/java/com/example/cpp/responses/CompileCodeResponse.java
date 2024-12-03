package com.example.cpp.responses;

import com.example.cpp.models.TestCase;
import com.example.cpp.models.TestResult;
import lombok.*;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompileCodeResponse {
    private List<TestResult> testResults;
    private boolean isTimeLimit;
}
