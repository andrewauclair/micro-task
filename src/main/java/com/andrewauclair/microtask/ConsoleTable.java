// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask;

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

	List<String> headers = new ArrayList<>();
	List<List<String>> output = new ArrayList<>();

	List<Alignment> alignments = new ArrayList<>();

	boolean alternateColors = false;
	private int spacing = 2;

	public void setHeaders(String... headers) {
		this.headers = Arrays.asList(headers);
	}

	public void setColumnAlignment(Alignment... alignments) {
		this.alignments = Arrays.asList(alignments);
	}

	public void setCellSpacing(int spacing) {
		this.spacing = spacing;
	}

	public void enableAlternatingColors() {
		alternateColors = true;
	}

	public void addRow(String... cells) {
		output.add(Arrays.asList(cells));
	}

	public void print() {
		String space = " ".repeat(spacing);

		List<Integer> widths = new ArrayList<>();

		for (final String header : headers) {
			widths.add(header.length());
		}

		for (final List<String> row : output) {
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

		if (headers.size() > 0) {
			for (int i = 0; i < widths.size(); i++) {
				String header = headers.get(i);

				System.out.print(String.format("%-" + widths.get(i) + "s", ANSI_UNDERLINE + header + ANSI_RESET));

				if (i + 1 < widths.size()) {
					System.out.print(space);
				}
			}
			System.out.println();
		}

		int rowCount = 0;

		for (final List<String> row : output) {
			rowCount++;

			if (rowCount % 2 != 0 && alternateColors) {
				System.out.print("\u001B[49;5;237m");
			}

			for (int i = 0; i < row.size(); i++) {
				String cell = row.get(i);

				String format;
				if (getAlignment(i) == Alignment.LEFT) {
					format = "%-" + widths.get(i) + "s";
				}
				else {
					format = "%" + widths.get(i) + "s";
				}

				System.out.print(String.format(format, cell));

				if (i + 1 < row.size()) {
					System.out.print(space);
				}
			}

			if (rowCount % 2 != 0 && alternateColors) {
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
