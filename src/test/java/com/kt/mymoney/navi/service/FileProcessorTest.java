package com.kt.mymoney.navi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.DataFormatException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;

import com.kt.navi.mymoney.data.PortfolioData;
import com.kt.navi.mymoney.services.FileProcessor;
import com.kt.navi.mymoney.services.InvestorPortfolioImpl;
import com.kt.navi.mymoney.services.InvestorPortfolioInterface;


public class FileProcessorTest {
    @Mock private InvestorPortfolioInterface investorPortolio;
    @Mock private PortfolioData portfolioData;
    @Spy private FileProcessor fileProcessor;

    @BeforeEach
    public void setUp(){
        portfolioData = new PortfolioData();
        investorPortolio = new InvestorPortfolioImpl(portfolioData);
        fileProcessor = new FileProcessor(investorPortolio);
    }
    @Test
    void testExecuteCommandsFromFileWithInvalidFile() {
      assertThrows(
          IOException.class,
          () -> fileProcessor.readFileAndExecuteCommands("inputFile"),
          "Expected Allocate method to throw Exception, but it didn't.");
    }

    @Test
    public void testReadFileAndExecuteCommands() throws IOException, DataFormatException {
        String inputFile =
        Objects.requireNonNull(this.getClass().getClassLoader().getResource("testInputFile1"))
            .getFile();
        String outputFile =
            Objects.requireNonNull(this.getClass().getClassLoader().getResource("testOutputFile1"))
                .getFile();
        List<String> output = fileProcessor.readFileAndExecuteCommands(inputFile);
        try (Stream<String> lines = Files.lines(Paths.get(outputFile))) {
        String expectedResult = lines.map(String::trim).collect(Collectors.joining(";"));
        String result =
            output.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .collect(Collectors.joining(";"));
        assertEquals(expectedResult, result);
        }
    }
}
