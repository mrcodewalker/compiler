package com.example.cpp.execution;

import com.example.cpp.config.LanguageConfig;
import com.example.cpp.config.SecurityConfig;
import com.example.cpp.models.TestCase;
import com.example.cpp.models.TestResult;
import com.example.cpp.provider.CompilerVersion;
import com.example.cpp.provider.Language;


import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class ProgramGradingSystem {
    private static final long TIME_LIMIT_MS = 1000; // 2 seconds
    private static final long MEMORY_LIMIT_BYTES = 256 * 1024 * 1024; // 256MB

    public static List<TestResult> gradeSubmission(String sourceCode, List<TestCase> testCases,
                                                   Language language, CompilerVersion compilerVersion,
                                                   Long timeLimit) {
        List<TestResult> results = new ArrayList<>();

        // Kiểm tra bảo mật
        if (!SecurityConfig.isCodeSecure(sourceCode, language)) {
            results.add(new TestResult(false, "Potential Malicious Code Detected", 0, 0,0,false, 0L));
            return results;
        }

        String fileName = "Solution" + LanguageConfig.getFileExtension(language);
        Path filePath = Paths.get(fileName);

        try {
            Files.writeString(filePath, sourceCode);

            Process compileProcess = null;
            if (language != Language.PYTHON) {
                String[] compileCommand = LanguageConfig.getCompilerCommand(compilerVersion);
                if (compileCommand != null) {
                    compileProcess = new ProcessBuilder(compileCommand)
                            .redirectErrorStream(true)
                            .start();

                    String compileOutput = captureProcessOutput(compileProcess);
                    int compileResult = compileProcess.waitFor();
                    if (compileResult != 0) {
                        results.add(new TestResult(false, "Compilation Error: " + compileOutput, 0, 0,0,false, 0L));
                        return results;
                    }
                }
            }

            // Chuẩn bị lệnh chạy
            String[] runCommand = LanguageConfig.getRunCommand(language);
            ProcessBuilder runPB = new ProcessBuilder(runCommand)
                    .redirectErrorStream(true);

            // Chạy từng test case
            for (TestCase testCase : testCases) {
                Process runProcess = runPB.start();
                TestResult result = executeAndGrade(runProcess, testCase.getInput(), testCase.getExpectedOutput(), timeLimit);
                results.add(result);
                if (!result.passed
                        || result.getExecutionTime()>=timeLimit
                        || result.timeLimit)
                    return results;
            }

            return results;

        } catch (Exception e) {
            results.add(new TestResult(false, "Error: " + e.getMessage(), 0, 0,0,false, 0L));
            return results;
        } finally {
            cleanup(fileName, language);
        }
    }

    private static String captureProcessOutput(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            return output.toString();
        }
    }

    private static TestResult executeAndGrade(Process process, String input, String expectedOutput, Long timeLimit)
            throws Exception {
        long startTime = System.currentTimeMillis();
        Runtime runtime = Runtime.getRuntime();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        try (OutputStream os = process.getOutputStream()) {
            os.write(input.getBytes());
            os.flush();
        }

        Future<String> outputFuture = Executors.newSingleThreadExecutor().submit(() -> {
            return captureProcessOutput(process);
        });

        try {
            String actualOutput = outputFuture.get(timeLimit, TimeUnit.MILLISECONDS);
            long endTime = System.currentTimeMillis();
            long executionTime = endTime-startTime;
            long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
            long memoryUsed = memoryAfter - memoryBefore;
            if (normalizeString(actualOutput).equals(normalizeString(expectedOutput))) {
                return new TestResult(true, "Passed", startTime,endTime,executionTime, false, memoryUsed);
            } else {
                return new TestResult(false,
                        String.format("Wrong Answer. Expected: %s, Got: %s",
                                expectedOutput.replace("\n","").trim(), actualOutput.replace("\n","").trim()),
                        startTime, endTime, executionTime, false, memoryUsed);
            }
        } catch (TimeoutException e) {
            long endTime = System.currentTimeMillis();
            long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
            long memoryUsed = memoryAfter - memoryBefore;
            process.destroyForcibly();
            return new TestResult(false, "Time Limit Exceeded", startTime, startTime+timeLimit, timeLimit, true, memoryUsed);
        }
    }
    private static void cleanup(String fileName, Language language) {
        try {
            Files.deleteIfExists(Paths.get(fileName));
            if (language != Language.JAVA && language != Language.PYTHON) {
                Files.deleteIfExists(Paths.get("Solution"));
            } else if (language == Language.JAVA) {
                Files.deleteIfExists(Paths.get("Solution.class"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static String normalizeString(String str) {
        return str.trim().replaceAll("\\s+", " ").replaceAll("\\r\\n", "\n");
    }

    public static void main(String[] args) {
        List<TestCase> testCases = new ArrayList<>();
        testCases.add(new TestCase("2 3\n", "5\n"));
        testCases.add(new TestCase("5 7\n", "12\n"));

        // Test C++17 program
        String cppCode =
                "#include <iostream>\n" +
                        "using namespace std;\n" +
                        "int main() {\n" +
                        "    int a, b;\n" +
                        "    cin >> a >> b;\n" +
                        "    cout << a + b << endl;\n" +
                        "    return 0;\n" +
                        "}\n";

        System.out.println("\nTesting C++17 program:");
//        List<TestResult> cppResults = runTestCases(cppCode, testCases, Language.CPP, CompilerVersion.CPP_17);
//        printResults(cppResults);
    }

    private static void printResults(List<TestResult> results) {
        for (int i = 0; i < results.size(); i++) {
            TestResult result = results.get(i);
            System.out.println("Test case " + (i + 1) + ":");
            System.out.println("Status: " + result.message);
            System.out.println("Execution time: " + result.executionTime + "ms");
            System.out.println("Time limit exceeded: " + result.timeLimit);
            System.out.println();
        }
    }
}