package com.kt.navi.mymoney;

import java.util.InputMismatchException;

import org.jline.utils.Log;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.annotation.Order;

import com.kt.navi.mymoney.services.FileProcessor;

import lombok.extern.log4j.Log4j2;


@SpringBootApplication
@Order(-1)
@Log4j2
public class MyMoneyApp implements CommandLineRunner{
	final FileProcessor fileProcessor;
	
	public MyMoneyApp(FileProcessor fileProcessor) {
		this.fileProcessor = fileProcessor;
	}

	public static void main(String[] args) {
		SpringApplication.run(MyMoneyApp.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		if(args.length != 1){
			Log.error("need only one argument as filename. Not matching the expected input");
			throw new InputMismatchException("please specify filename");
		}
		String filename = args[0];
		fileProcessor.readFileAndExecuteCommands(filename);
		System.exit(0);
	}

}
