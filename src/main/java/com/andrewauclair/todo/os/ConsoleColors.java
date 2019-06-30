// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.os;

import java.io.PrintStream;

public class ConsoleColors {
	// TODO I could write some simple tests for this stuff
	public static void print(PrintStream stream, ConsoleColor color, String message) {
		stream.print(color);
		stream.print(message);
		stream.print(ConsoleColor.ANSI_RESET);
	}

	public static void println(PrintStream stream, ConsoleColor color, String message) {
		print(stream, color, message);
		stream.println();
	}

	public enum ConsoleColor {
		ANSI_RESET("\u001B[0m"),
		ANSI_BLACK("\u001B[30m"),
		ANSI_RED("\u001B[31m"),
		ANSI_GREEN("\u001B[32m"),
		ANSI_YELLOW("\u001B[33m"),
		ANSI_BLUE("\u001B[34m"),
		ANSI_PURPLE("\u001B[35m"),
		ANSI_CYAN("\u001B[36m"),
		ANSI_WHITE("\u001B[37m");

		private String str;

		ConsoleColor(String str) {
			this.str = str;
		}

		@Override
		public String toString() {
			return str;
		}
	}
}
