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
	private final OSInterface osInterface;

	public enum Alignment {
		LEFT,
		RIGHT
	}

	List<String> headers = new ArrayList<>();
	List<List<String>> cells = new ArrayList<>();

	List<ConsoleBackgroundColor> rowColors = new ArrayList<>();

	List<Alignment> alignments = new ArrayList<>();

	boolean alternateColors = false;
	private int spacing = 2;
	private int rowLimit = 0;

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

	public void setRowLimit(int limit) {
		rowLimit = limit;
	}

	public void enableAlternatingColors() {
		alternateColors = true;
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

		List<Integer> widths = new ArrayList<>();

		for (final String header : headers) {
			widths.add(header.length());
		}

		for (final List<String> row : cells) {
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

		int terminalWidth = osInterface.getTerminalWidth();

		if (headers.size() > 0) {
			String line = "";

			for (int i = 0; i < widths.size(); i++) {
				String header = headers.get(i);

				line += String.format("%-" + widths.get(i) + "s", ANSI_UNDERLINE + header + ANSI_RESET);

				if (i + 1 < widths.size()) {
					line += space;
				}
			}
			if (line.length() > terminalWidth) {
				line = line.substring(0, terminalWidth);
			}
			System.out.println(line);
		}

		int rowCount = 0;

		for (final List<String> row : cells) {
			if (rowCount >= rowLimit && rowLimit != 0) {
				break;
			}

			ConsoleBackgroundColor bgColor = rowColors.get(rowCount);

			rowCount++;

			if (bgColor != ConsoleBackgroundColor.ANSI_BG_BLACK) {
				System.out.print(bgColor);
			}
			else if (rowCount % 2 != 0 && alternateColors) {
				System.out.print(ConsoleBackgroundColor.ANSI_BG_GRAY);
			}

			String line = "";

			for (int i = 0; i < row.size(); i++) {
				String cell = row.get(i);

				String format;
				if (getAlignment(i) == Alignment.LEFT) {
					format = "%-" + widths.get(i) + "s";
				}
				else {
					format = "%" + widths.get(i) + "s";
				}

				line += String.format(format, cell);
//				System.out.print(String.format(format, cell));

				if (i + 1 < row.size()) {
//					System.out.print(space);
					line += space;
				}
			}

			if (line.replaceAll(" +$", "").length() > terminalWidth) {
				line = line.substring(0, terminalWidth - 3) + "...";
			}
			else if (line.length() > terminalWidth) {
				line = line.substring(0, terminalWidth);
			}

			System.out.print(line);

			if ((rowCount % 2 != 0 && alternateColors) ||
					bgColor != ConsoleBackgroundColor.ANSI_BG_BLACK) {
				System.out.print(ANSI_RESET);
			}
			System.out.println();
		}
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
