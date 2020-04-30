// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask;

import com.andrewauclair.microtask.os.ConsoleColors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import static com.andrewauclair.microtask.ConsoleTable.Alignment.LEFT;
import static com.andrewauclair.microtask.ConsoleTable.Alignment.RIGHT;
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;
import static org.assertj.core.api.Assertions.assertThat;

public class ConsoleTable_Test {
	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private final PrintStream printStream = new PrintStream(outputStream);

	private final PrintStream originalSystemOut = System.out;

	private final ConsoleTable table = new ConsoleTable();

	@BeforeEach
	void setup() {
		System.setOut(printStream);
	}

	@AfterEach
	void teardown() {
		System.setOut(originalSystemOut);
	}

	@Test
	void formatted_row_output() {
		table.addRow("1", "one", "data", "data", "data");
		table.addRow("2", "two", "bigger", "more", "here");
		table.addRow("3", "three", "", "", "");
		table.addRow("4", "four", "bigger", "", "last");

		table.print();

		assertOutput(
				"1  one    data    data  data",
				"2  two    bigger  more  here",
				"3  three                    ",
				"4  four   bigger        last"
		);
	}

	@Test
	void column_headers() {
		table.setHeaders("ID", "One", "Two", "Three", "Four");

		table.addRow("1", "one", "data", "data", "data");
		table.addRow("2", "two", "bigger", "more", "here");
		table.addRow("3", "three", "", "", "");
		table.addRow("4", "four", "bigger", "", "last");

		table.print();

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				u + "ID" + r + "  " + u + "One" + r + "  " + u + "Two" + r + "  " + u + "Three" + r + "  " + u + "Four" + r,
//				"ID  One    Two     Three  Four",
				"1   one    data    data   data",
				"2   two    bigger  more   here",
				"3   three                     ",
				"4   four   bigger         last"
		);
	}

	@Test
	void alternating_row_color() {
		table.enableAlternatingColors();

		table.addRow("1", "one", "data", "data", "data");
		table.addRow("2", "two", "bigger", "more", "here");
		table.addRow("3", "three", "", "", "");
		table.addRow("4", "four", "bigger", "", "last");

		table.print();

		assertOutput(
				"\u001B[49;5;237m1  one    data    data  data" + ANSI_RESET,
				"2  two    bigger  more  here",
				"\u001B[49;5;237m3  three                    " + ANSI_RESET,
				"4  four   bigger        last"
		);
	}

	@Test
	void justify_columns_left_and_right() {
		table.setColumnAlignment(LEFT, RIGHT, LEFT, RIGHT);

		table.addRow("a", "b", "c", "d");
		table.addRow("aa", "bb", "cc", "dd");
		table.addRow("aaa", "bbb", "ccc", "ddd");
		table.addRow("aaaa", "bbbb", "cccc", "dddd");

		table.print();

		assertOutput(
				"a        b  c        d",
				"aa      bb  cc      dd",
				"aaa    bbb  ccc    ddd",
				"aaaa  bbbb  cccc  dddd"
		);
	}

	@Test
	void column_alignment_defaults_to_left_when_going_past_end_of_array() {
		table.setColumnAlignment(LEFT, RIGHT, LEFT);

		table.addRow("a", "b", "c", "d");
		table.addRow("aa", "bb", "cc", "dd");
		table.addRow("aaa", "bbb", "ccc", "ddd");
		table.addRow("aaaa", "bbbb", "cccc", "dddd");

		table.print();

		assertOutput(
				"a        b  c     d   ",
				"aa      bb  cc    dd  ",
				"aaa    bbb  ccc   ddd ",
				"aaaa  bbbb  cccc  dddd"
		);
	}

	@Test
	void modify_spacing_between_cells() {
		table.setHeaders("ID", "One", "Two", "Three", "Four");
		table.setCellSpacing(1);

		table.addRow("1", "one", "data", "data", "data");
		table.addRow("2", "two", "bigger", "more", "here");
		table.addRow("3", "three", "", "", "");
		table.addRow("4", "four", "bigger", "", "last");

		table.print();

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				u + "ID" + r + " " + u + "One" + r + " " + u + "Two" + r + " " + u + "Three" + r + " " + u + "Four" + r,
//				"ID One   Two    Three Four",
				"1  one   data   data  data",
				"2  two   bigger more  here",
				"3  three                  ",
				"4  four  bigger       last"
		);
	}

	protected void assertOutput(String... lines) {
		assertOutput(outputStream, lines);
	}

	public static void assertOutput(OutputStream outputStream, String... lines) {
		StringBuilder output = new StringBuilder();

		for (String line : lines) {
			output.append(line);
			output.append(Utils.NL);
		}

		assertThat(outputStream.toString()).isEqualTo(output.toString());
	}
}
