// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask;

import com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor;
import com.andrewauclair.microtask.os.OSInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_UNDERLINE;

public class ConsoleTable {
	public enum Alignment {
		LEFT,
		RIGHT
	}

	private final OSInterface osInterface;

	List<String> headers = new ArrayList<>();
	List<List<String>> cells = new ArrayList<>();

	List<ConsoleBackgroundColor> rowColors = new ArrayList<>();

	List<Alignment> alignments = new ArrayList<>();

	boolean alternateColors = false;
	boolean wordWrap = false;
	boolean printLastRows = false;
	private int spacing = 2;
	private int rowLimit = Integer.MAX_VALUE;

	public ConsoleTable(OSInterface osInterface) {
		this.osInterface = osInterface;
	}

	public void setHeaders(String... headers) {
		this.headers = Arrays.asList(headers);
	}

	public void setColumnAlignment(Alignment... alignments) {
		this.alignments = Arrays.asList(alignments);
	}

	public void setCellSpacing(int spacing) {
		this.spacing = spacing;
	}

	public void setRowLimit(int limit, boolean printLastRows) {
		rowLimit = limit;
		this.printLastRows = printLastRows;
	}

	public void enableAlternatingColors() {
		alternateColors = true;
	}

	public void enableWordWrap() {
		wordWrap = true;
	}

	public void addRow(String... cells) {
		addRow(ConsoleBackgroundColor.ANSI_BG_BLACK, cells);
	}

	public void addRow(ConsoleBackgroundColor bgColor, String... cells) {
		rowColors.add(bgColor);
		this.cells.add(Arrays.asList(cells));
	}

	public void print() {
		String space = " ".repeat(spacing);

		List<Integer> widths = getColumnWidths();

		int startRow = getStartRow();

		int terminalWidth = osInterface.getTerminalWidth();

		if (headers.size() > 0) {
			StringBuilder line = new StringBuilder();

			for (int i = 0; i < widths.size(); i++) {
				String header = headers.get(i);

				line.append(ANSI_UNDERLINE);
				line.append(header);
				line.append(ANSI_RESET);

				if (widths.get(i) - header.length() > 0) {
					line.append(String.format("%" + (widths.get(i) - header.length()) + "s", " "));
				}

				if (i + 1 < widths.size()) {
					line.append(space);
				}
			}
			if (line.length() > terminalWidth) {
				line = new StringBuilder(line.substring(0, terminalWidth));
			}
			System.out.println(line);
		}

		int rowCount = 0;

		for (int rowNum = startRow; rowNum < cells.size(); rowNum++) {
			List<String> row = cells.get(rowNum);

			if (rowCount >= rowLimit && rowLimit != 0) {
				break;
			}

			ConsoleBackgroundColor bgColor = rowColors.get(rowCount + startRow);

			rowCount++;

			if (bgColor != ConsoleBackgroundColor.ANSI_BG_BLACK) {
				System.out.print(bgColor);
			}
			else if (rowCount % 2 != 0 && alternateColors) {
				System.out.print(ConsoleBackgroundColor.ANSI_BG_GRAY);
			}

			StringBuilder line = new StringBuilder();
			int prelength = 0;

			for (int i = 0; i < row.size(); i++) {
				String cell = row.get(i);

				String format;
				if (getAlignment(i) == Alignment.LEFT) {
					format = "%-" + widths.get(i) + "s";
				}
				else {
					format = "%" + widths.get(i) + "s";
				}

				String cellLine = String.format(format, cell);
				if (i == row.size() - 1 && wordWrap && (line.length() + cellLine.trim().length() > terminalWidth)) {
					prelength = line.length();
				}

				line.append(cellLine);

				if (i + 1 < row.size()) {
					line.append(space);
				}
			}

			if (wordWrap && line.toString().trim().length() > terminalWidth) {
				int lastSpace = line.substring(0, terminalWidth + 1).lastIndexOf(' ');

				String currentline = line.substring(0, lastSpace);
				System.out.println(String.format("%-" + terminalWidth + "s", currentline));

				String nextLine = " ".repeat(prelength) + line.substring(lastSpace + 1);

				while (nextLine.length() > terminalWidth) {
					lastSpace = nextLine.substring(0, terminalWidth + 1).lastIndexOf(' ');
					currentline = nextLine.substring(0, lastSpace);
					nextLine = " ".repeat(prelength) + nextLine.substring(lastSpace + 1);

					System.out.println(String.format("%-" + terminalWidth + "s", currentline));
				}
				System.out.print(String.format("%-" + terminalWidth + "s", nextLine));
			}
			else {
				if (line.toString().replaceAll(" +$", "").length() > terminalWidth) {
					line = new StringBuilder(line.substring(0, terminalWidth - 3) + "...");
				}
				else if (line.length() > terminalWidth) {
					line = new StringBuilder(line.substring(0, terminalWidth));
				}

				System.out.print(line);
			}

			if ((rowCount % 2 != 0 && alternateColors) ||
					bgColor != ConsoleBackgroundColor.ANSI_BG_BLACK) {
				System.out.print(ANSI_RESET);
			}
			System.out.println();
		}
	}

	private List<Integer> getColumnWidths() {
		List<Integer> widths = new ArrayList<>();

		for (final String header : headers) {
			widths.add(header.length());
		}

		int startRow = getStartRow();

		for (int rowNum = startRow; rowNum < cells.size(); rowNum++) {
			List<String> row = cells.get(rowNum);

			for (int i = 0; i < row.size(); i++) {
				if (widths.size() < i + 1) {
					widths.add(0);
				}

				String cell = row.get(i);

				if (cell.length() > widths.get(i)) {
					widths.set(i, cell.length());
				}
			}
		}
		return widths;
	}

	private int getStartRow() {
		return cells.size() > rowLimit && printLastRows ? cells.size() - rowLimit : 0;
	}

	private Alignment getAlignment(int column) {
		if (alignments.isEmpty()) {
			return Alignment.LEFT;
		}
		if (column < alignments.size()) {
			return alignments.get(column);
		}
		return Alignment.LEFT;
	}
}
