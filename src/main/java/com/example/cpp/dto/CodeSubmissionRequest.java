package com.example.cpp.dto;

import com.example.cpp.models.TestCase;
import com.example.cpp.provider.CompilerVersion;
import com.example.cpp.provider.Language;
import lombok.*;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CodeSubmissionRequest {
    private String sourceCode;
    private List<TestCase> testCases;
    private Language language;
    private CompilerVersion compilerVersion;
    private Long timeLimit;
    private Long memoryUsed;
}