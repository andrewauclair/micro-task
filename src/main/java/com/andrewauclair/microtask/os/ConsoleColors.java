// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.os;

import java.io.PrintStream;

// See this link for info: http://www.lihaoyi.com/post/BuildyourownCommandLinewithANSIescapecodes.html
public final class ConsoleColors {
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BOLD = "\u001B[1m";
	public static final String ANSI_UNDERLINE = "\u001B[4m";
	public static final String ANSI_REVERSED = "\u001B[7m";

	public static void println(PrintStream stream, ConsoleForegroundColor color, String message) {
		print(stream, color, message);
		stream.println();
	}

	private static void print(PrintStream stream, ConsoleForegroundColor color, String message) {
		stream.print(color);
		stream.print(message);
		stream.print(ANSI_RESET);
	}

	public static void println(PrintStream stream, ConsoleForegroundColor fgColor, ConsoleBackgroundColor bgColor, String message) {
		print(stream, fgColor, bgColor, message);
		stream.println();
	}

	private static void print(PrintStream stream, ConsoleForegroundColor fgColor, ConsoleBackgroundColor bgColor, String message) {
		stream.print(bgColor);
		stream.print(fgColor);
		stream.print(message);
		stream.print(ANSI_RESET);
	}

	public enum ConsoleForegroundColor {
		ANSI_FG_BLACK("\u001B[30m"),
		ANSI_FG_RED("\u001B[31m"),
		ANSI_FG_GREEN("\u001B[32m"),
		ANSI_FG_YELLOW("\u001B[33m"),
		ANSI_FG_BLUE("\u001B[34m"),
		ANSI_FG_PURPLE("\u001B[35m"),
		ANSI_FG_CYAN("\u001B[36m"),
		ANSI_FG_WHITE("\u001B[37m"),
		ANSI_FG_BRIGHT_BLACK("\u001B[30;1m"),
		ANSI_FG_BRIGHT_RED("\u001B[31;1m"),
		ANSI_FG_BRIGHT_GREEN("\u001B[32;1m"),
		ANSI_FG_BRIGHT_YELLOW("\u001B[33;1m"),
		ANSI_FG_BRIGHT_BLUE("\u001B[34;1m"),
		ANSI_FG_BRIGHT_PURPLE("\u001B[35;1m"),
		ANSI_FG_BRIGHT_CYAN("\u001B[36;1m"),
		ANSI_FG_BRIGHT_WHITE("\u001B[37;1m");

		private final String str;

		ConsoleForegroundColor(String str) {
			this.str = str;
		}

		@Override
		public String toString() {
			return str;
		}
	}

	public enum ConsoleBackgroundColor {
		ANSI_BG_BLACK("\u001B[40m"),
		ANSI_BG_RED("\u001B[41m"),
		ANSI_BG_GREEN("\u001B[42m"),
		ANSI_BG_YELLOW("\u001B[43m"),
		ANSI_BG_BLUE("\u001B[44m"),
		ANSI_BG_PURPLE("\u001B[45m"),
		ANSI_BG_CYAN("\u001B[46m"),
		ANSI_BG_WHITE("\u001B[47m"),
		ANSI_BG_BRIGHT_BLACK("\u001B[40;1m"),
		ANSI_BG_BRIGHT_RED("\u001B[41;1m"),
		ANSI_BG_BRIGHT_GREEN("\u001B[42;1m"),
		ANSI_BG_BRIGHT_YELLOW("\u001B[43;1m"),
		ANSI_BG_BRIGHT_BLUE("\u001B[44;1m"),
		ANSI_BG_BRIGHT_PURPLE("\u001B[45;1m"),
		ANSI_BG_BRIGHT_CYAN("\u001B[46;1m"),
		ANSI_BG_BRIGHT_WHITE("\u001B[47;1m"),


		//		ANSI_BG_GRAY("\u001B[48;5;234m");
		ANSI_BG_GRAY("\u001B[44m");

		private final String str;

		ConsoleBackgroundColor(String str) {
			this.str = str;
		}

		@Override
		public String toString() {
			return str;
		}
	}
}
