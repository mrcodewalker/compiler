package com.example.cpp.controller;

import com.example.cpp.dto.CodeSubmissionRequest;
import com.example.cpp.execution.ProgramGradingSystem;
import com.example.cpp.models.TestResult;
import com.example.cpp.provider.CompilerVersion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequiredArgsConstructor
public class SubmitController {
    @PostMapping("/submit")
    public List<TestResult> submitCode(@RequestBody CodeSubmissionRequest request) {
        return ProgramGradingSystem.gradeSubmission(
                request.getSourceCode(),
                request.getTestCases(),
                request.getLanguage(),
                request.getCompilerVersion()
        );
    }
    @GetMapping("/compiler-versions")
    public CompilerVersion[] getAvailableVersions() {
        return CompilerVersion.values();
    }
}
