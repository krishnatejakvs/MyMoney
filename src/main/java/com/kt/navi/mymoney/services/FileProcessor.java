package com.kt.navi.mymoney.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;

import org.jline.utils.Log;
import org.springframework.stereotype.Service;

import com.kt.navi.mymoney.enums.Commands;

@Service
public class FileProcessor {
    
    final InvestorPortfolioInterface investorPortolio;
    static final int allocateLimit = 3;
    static final int sipLimit = 3;
    static final int changeLimit = 3;
    static final int balanceLimit = 1;
    public FileProcessor(InvestorPortfolioInterface investorPortolio) {
        this.investorPortolio = investorPortolio;
    }

    /**
     * The method receives the filename, it reads the lines and forwards to commandLineChecker 
     * for validation
     * 
     * @param filename
     * @return 
     * @throws DataFormatException
     * 
     */
    public List<String> readFileAndExecuteCommands(String filename) throws IOException, DataFormatException{
        List<String> outputs = new ArrayList<>();
        String output;
        try {
            List<String> allLines = Files.readAllLines(Paths.get(filename));
			for (String line : allLines) {
				// System.out.println(line);
                output = parseCommandsFromLine(line);
                if (output != null) {
                    outputs.add(output);
                }
			}
		} catch (IOException e) {
			throw new IOException("Invalid File. Please check the path & name for input file provided. filename is " + filename );
		}
        outputs.stream().filter(Objects::nonNull).forEach(System.out::println);
        return outputs;
    }

    /**
     * The function parses the line and will execute the commands
     * @param line
     * @throws DataFormatException
     */
    public String parseCommandsFromLine(String line) throws DataFormatException{
        String output = null;
        String delimeter = " ";
        String[] commandsAndParams = line.split(delimeter);
        Commands command = Commands.valueOf(commandsAndParams[0]);
        try {
            switch(command){
                case ALLOCATE:
                    validateCommandAndInputSize(commandsAndParams,allocateLimit);
                    List<Double> allocations = StringToDoubleList(commandsAndParams, allocateLimit, 1);
                    investorPortolio.allocate(allocations);
                    break;
                case SIP:
                    validateCommandAndInputSize(commandsAndParams,sipLimit);
                    List<Double> sip = StringToDoubleList(commandsAndParams, sipLimit, 1);
                    investorPortolio.sip(sip);
                    break;
                case CHANGE:
                    validateCommandAndInputSize(commandsAndParams, changeLimit+1);
                    List<Double> changeRate =
                    Arrays.stream(commandsAndParams)
                            .skip(1)
                            .limit(changeLimit)
                            .map(str -> Double.parseDouble(str.replace("%", "")))
                            .collect(Collectors.toList());
                    Month month = Month.valueOf(commandsAndParams[changeLimit +1]);
                    investorPortolio.change(changeRate, month);
                    break;
                case BALANCE:
                    validateCommandAndInputSize(commandsAndParams, balanceLimit);
                    month = Month.valueOf(commandsAndParams[balanceLimit]);
                    output = investorPortolio.balance(month);
                    break;
                case REBALANCE:
                    output = investorPortolio.reBalance();
                    break;
                default:
                    throw new DataFormatException("Unknown Command " + command);
            }    
        } catch (Exception e) {
            System.out.println(
          "Error Occurred while processing " + String.join(" ", commandsAndParams) + e.getMessage());
        }
        return output;    
    }

    private List<Double> StringToDoubleList(String[] commandsAndParams, int limit, int skip) {
        return Arrays.stream(commandsAndParams)
        .skip(skip)
        .limit(limit)
        .map(Double::parseDouble)
        .collect(Collectors.toList());
    }

    private void validateCommandAndInputSize(String[] commandsAndParams, int size){
        if (commandsAndParams.length != size + 1) {
            throw new InputMismatchException(
                "command is incorrect. please check " + String.join(" ", commandsAndParams));
       }
    }
}
