// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Console {
	private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

	public Console() {

	}

	public String readInput(String prompt) {
		System.out.print(prompt);
		System.out.print(">");

		try {
			return reader.readLine();
		}
		catch (IOException e) {
			e.printStackTrace(System.out);
		}
		return "";
	}

	public static void main(String[] args) {
		Console console = new Console();
		while (true) {
			console.readInput("340");
		}
	}
}
