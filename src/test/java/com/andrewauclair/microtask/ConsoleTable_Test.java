// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask;

import com.andrewauclair.microtask.os.ConsoleColors;
import com.andrewauclair.microtask.os.OSInterface;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import static com.andrewauclair.microtask.ConsoleTable.Alignment.LEFT;
import static com.andrewauclair.microtask.ConsoleTable.Alignment.RIGHT;
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ConsoleTable_Test {
	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private final PrintStream printStream = new PrintStream(outputStream);

	private final PrintStream originalSystemOut = System.out;

	private final OSInterface osInterface = Mockito.mock(OSInterface.class);
	private final ConsoleTable table = new ConsoleTable(osInterface);

	@BeforeEach
	void setup() {
		System.setOut(printStream);

		Mockito.when(osInterface.getTerminalWidth()).thenReturn(80);
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
				u + "ID" + r + "  " + u + "One" + r + "    " + u + "Two" + r + "     " + u + "Three" + r + "  " + u + "Four" + r,
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
				ANSI_BG_GRAY + "1  one    data    data  data" + ANSI_RESET,
				"2  two    bigger  more  here",
				ANSI_BG_GRAY + "3  three                    " + ANSI_RESET,
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
				u + "ID" + r + " " + u + "One" + r + "   " + u + "Two" + r + "    " + u + "Three" + r + " " + u + "Four" + r,
//				"ID One   Two    Three Four",
				"1  one   data   data  data",
				"2  two   bigger more  here",
				"3  three                  ",
				"4  four  bigger       last"
		);
	}

	@Test
	void set_a_limit_for_how_many_rows_are_printed() {
		table.setHeaders("ID", "One", "Two", "Three", "Four");
		table.setRowLimit(2, false);

		table.addRow("1", "one", "data", "data", "data");
		table.addRow("2", "two", "bigger", "more", "here");
		table.addRow("3", "three", "", "", "");
		table.addRow("4", "four", "bigger", "", "last");

		table.print();

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				u + "ID" + r + "  " + u + "One" + r + "    " + u + "Two" + r + "     " + u + "Three" + r + "  " + u + "Four" + r,
//				"ID  One    Two     Three  Four",
				"1   one    data    data   data",
				"2   two    bigger  more   here"
		);
	}

	@Test
	void add_row_with_a_different_color() {
		table.setHeaders("ID", "One", "Two", "Three", "Four");
		table.setRowLimit(2, false);

		table.addRow("1", "one", "data", "data", "data");
		table.addRow(ANSI_BG_BLUE, false, "2", "two", "bigger", "more", "here");
		table.addRow("3", "three", "", "", "");
		table.addRow("4", "four", "bigger", "", "last");

		table.print();

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				u + "ID" + r + "  " + u + "One" + r + "    " + u + "Two" + r + "     " + u + "Three" + r + "  " + u + "Four" + r,
//				"ID  One    Two     Three  Four",
				"1   one    data    data   data",
				ANSI_BG_BLUE + "2   two    bigger  more   here" + ANSI_RESET
		);
	}

	@Test
	void wrapped_lines_are_counted_for_row_limit() {
		table.setRowLimit(3, false);
		table.enableWordWrap();

		table.addRow("data", "this really long string of data in the last column will require wrapping around in the cell when it is displayed");
		table.addRow("data", "this is a normal string");
		table.addRow("data", "this is a normal string");

		table.print();

		assertOutput(
				"data  this really long string of data in the last column will require wrapping ",
				"      around in the cell when it is displayed                                  ",
				"data  this is a normal string                                                  "
		);
	}

	@Test
	void fits_height_when_all_lines_wrap() {
		table.setRowLimit(5, false);
		table.enableWordWrap();
		table.enableAlternatingColors();

		table.addRow("data", "this really long string of data in the last column will require wrapping around in the cell when it is displayed");
		table.addRow("data", "this really long string of data in the last column will require wrapping around in the cell when it is displayed");
		table.addRow("data", "this really long string of data in the last column will require wrapping around in the cell when it is displayed");
		table.addRow("data", "this is a normal string");
		table.addRow("data", "this is a normal string");

		table.print();

		assertOutput(
				ANSI_BG_GRAY + "data  this really long string of data in the last column will require wrapping ",
				"      around in the cell when it is displayed                                  " + ANSI_RESET,
				"data  this really long string of data in the last column will require wrapping ",
				"      around in the cell when it is displayed                                  "
		);
	}

	@Test
	void word_wrap_the_last_column() {
		table.enableWordWrap();

		table.addRow("data", "this really long string of data in the last column will require wrapping around in the cell when it is displayed");
		table.addRow("data", "this is a normal string");

		table.print();

		assertOutput(
				"data  this really long string of data in the last column will require wrapping ",
				"      around in the cell when it is displayed                                  ",
				"data  this is a normal string                                                  "
		);
	}

	@Test
	void word_wrap_the_last_column_when_word_perfectly_ends_at_edge() {
		table.enableWordWrap();

		table.addRow("data", "this is a really long string of data in the last column which will require wrapping around in the cell when it is displayed");
		table.addRow("data", "this is a normal string");

		table.print();

		assertOutput(
				"data  this is a really long string of data in the last column which will       ",
				"      require wrapping around in the cell when it is displayed                 ",
				"data  this is a normal string                                                  "
		);
	}

	@Test
	void word_wrap_the_last_column_wraps_the_colors() {
		table.enableWordWrap();
		table.enableAlternatingColors();

		table.addRow("data", "this is a really long string of data in the last column which will require wrapping around in the cell when it is displayed");
		table.addRow("data", "this is a normal string");
		table.addRow("data", "this is a normal string");

		table.print();

		assertOutput(
				ANSI_BG_GRAY + "data  this is a really long string of data in the last column which will       ",
				"      require wrapping around in the cell when it is displayed                 " + ANSI_RESET,
				"data  this is a normal string                                                  ",
				ANSI_BG_GRAY + "data  this is a normal string                                                  " + ANSI_RESET
		);
	}

	@Test
	void wrap_multiple_times() {
		table.enableWordWrap();

		table.addRow("data", "This is a really long string of values that will wrap several times " +
				"1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, " +
				"31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50");
		table.addRow("data", "this is a normal string");

		table.print();

		assertOutput(
				"data  This is a really long string of values that will wrap several times 1, 2,",
				"      3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, ",
				"      23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,  ",
				"      41, 42, 43, 44, 45, 46, 47, 48, 49, 50                                   ",
				"data  this is a normal string                                                  "
		);
	}

	@Test
	void wrap_rows_when_first_cells_are_empty() {
		table.enableWordWrap();

		table.setHeaders("One", "Two", "Three", "Four");
		table.addRow("", "", "", "this is a longer string that will wrap around to the next line even with the spaces in front");

		table.print();

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				u + "One" + r + "  " + u + "Two" + r + "  " + u + "Three" + r + "  " + u + "Four" + r + "                          ",
				"                 this is a longer string that will wrap around to the next line",
				"                 even with the spaces in front                                 "
		);
	}

	@Test
	void print_the_last_rows_with_color() {
		table.setRowLimit(1, true);

		table.addRow("add");
		table.addRow("two");
		table.addRow(ANSI_BG_GREEN, false, "rows");

		table.print();

		assertOutput(
				ANSI_BG_GREEN + "rows" + ANSI_RESET
		);
	}

	@Test
	void header_spaces_out_based_on_data_width() {
		table.setHeaders("type", "data");

		table.addRow("one", "alpha");
		table.addRow("forty-five", "beta");

		table.print();

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				u + "type" + r + "        " + u + "data" + r + " ",
				"one         alpha",
				"forty-five  beta "
		);
	}

	@Test
	void full_rows_can_be_added_to_break_up_data_in_table() {
		table.setHeaders("type", "data");

		table.addRow("one", "alpha");
		table.addRow("forty-five", "beta");
		table.addRow(true, "Another Row of Data");
		table.addRow("a", "bravo");

		table.print();

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				u + "type" + r + "        " + u + "data" + r + " ",
				"one         alpha",
				"forty-five  beta ",
				"",
				"Another Row of Data",
				"a           bravo"
		);
	}

	@Test
	void row_limit_is_split_between_groups() {
		table.setRowLimit(11, false);
		table.setColumnAlignment(LEFT, RIGHT);

		table.setHeaders("type", "data");

		table.addRow("one", "1");
		table.addRow("two", "2");
		table.addRow("three", "3");
		table.addRow("four", "4");
		table.addRow("five", "5");

		table.addRow(true, "Group 2");

		table.addRow("six", "6");
		table.addRow("seven", "7");
		table.addRow("eight", "8");
		table.addRow("nine", "9");
		table.addRow("ten", "10");

		table.addRow(true, "Group 3");

		table.addRow("eleven", "11");
		table.addRow("twelve", "12");
		table.addRow("thirteen", "13");
		table.addRow("fourteen", "14");
		table.addRow("fifteen", "15");

		table.print();

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				u + "type" + r + "      " + u + "data" + r,
				"one          1",
				"two          2",
				"three        3",
				"four         4",
				"",
				"Group 2",
				"six          6",
				"seven        7",
				"",
				"Group 3",
				"eleven      11"
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
