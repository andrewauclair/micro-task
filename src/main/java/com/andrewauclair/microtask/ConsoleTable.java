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
	boolean showLastRows = true;

	private int spacing = 2;
	private int rowLimit = Integer.MAX_VALUE;

	public ConsoleTable(OSInterface osInterface) {
		this.osInterface = osInterface;
	}

	public void showFirstRows() {
		showLastRows = false;
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

		int terminalWidth = osInterface.getTerminalWidth() - 1;

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
		int rowsDisplayed = 0;

		for (int rowNum = startRow; rowNum < cells.size(); rowNum++) {
			List<String> row = cells.get(rowNum);

			if (rowsDisplayed >= rowLimit && rowLimit != 0) {
				break;
			}

			ConsoleBackgroundColor bgColor = rowColors.get(rowCount + startRow);

			rowCount++;

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

			List<String> lines = new ArrayList<>();

			if (wordWrap && rtrim(line.toString()).length() > terminalWidth) {
				int lastSpace = line.substring(0, terminalWidth + 1).lastIndexOf(' ');

				String currentline = line.substring(0, lastSpace);
				lines.add(String.format("%-" + terminalWidth + "s", currentline));

				rowsDisplayed++;

				String nextLine = " ".repeat(prelength) + line.substring(lastSpace + 1);

				while (nextLine.length() > terminalWidth) {
					lastSpace = nextLine.substring(0, terminalWidth + 1).lastIndexOf(' ');
					currentline = nextLine.substring(0, lastSpace);
					nextLine = " ".repeat(prelength) + nextLine.substring(lastSpace + 1);

					lines.add(String.format("%-" + terminalWidth + "s", currentline));
					rowsDisplayed++;
				}
				lines.add(String.format("%-" + terminalWidth + "s", nextLine));
			}
			else {
				if (line.toString().replaceAll(" +$", "").length() > terminalWidth) {
					line = new StringBuilder(line.substring(0, terminalWidth - 3) + "...");
				}
				else if (line.length() > terminalWidth) {
					line = new StringBuilder(line.substring(0, terminalWidth));
				}

				lines.add(line.toString());
			}
			rowsDisplayed++;

			// adding these new rows would go over the limit
			if (rowsDisplayed > rowLimit && rowLimit != 0) {
				continue;
			}

			if (bgColor != ConsoleBackgroundColor.ANSI_BG_BLACK) {
				System.out.print(bgColor);
			}
			else if (rowCount % 2 != 0 && alternateColors) {
				System.out.print(ConsoleBackgroundColor.ANSI_BG_GRAY);
			}

			for (int i = 0; i < lines.size(); i++) {
				if (i + 1 < lines.size()) {
					System.out.println(lines.get(i));
				}
				else {
					System.out.print(lines.get(i));
				}
			}

			if ((rowCount % 2 != 0 && alternateColors) ||
					bgColor != ConsoleBackgroundColor.ANSI_BG_BLACK) {
				System.out.print(ANSI_RESET);
			}
			System.out.println();
		}
	}

	private static String rtrim(String str) {
		int i = str.length() - 1;
		while (i >= 0 && Character.isWhitespace(str.charAt(i))) {
			i--;
		}
		return str.substring(0, i + 1);
	}

	public List<Integer> getColumnWidths() {
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
		if (!showLastRows) {
			return 0; // always start at the first row in this case
		}
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
