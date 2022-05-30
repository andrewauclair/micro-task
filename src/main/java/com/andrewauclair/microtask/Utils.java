// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.Task;

import java.io.IOException;
import java.io.PrintStream;

public final class Utils {
	public static final String NL = System.lineSeparator();

	private static final int SECONDS_IN_HOUR = 60 * 60;
	private static final int SECONDS_IN_MINUTE = 60;
	private static final int HOURS_IN_DAY = 8;
	private static final int HOURS_IN_WEEK = 40;

	public enum HighestTime {
		None,
		Second,
		Minute,
		Hour,
		Day,
		Week;

		public boolean isAtLeast(HighestTime highestTime) {
			return this.ordinal() >= highestTime.ordinal();
		}
	}

	public static String formatTime(long time, HighestTime highestTime) {
		long minsInHour = 60 * 60;
		long hours = time / minsInHour;
		long minutes = (time - (hours * minsInHour)) / 60;
		long seconds = (time - (hours * minsInHour) - (minutes * 60));

		long days = hours / 8;
		hours = hours - (days * 8);

		long weeks = days / 5;
		days = days - (weeks * 5);

		String output = "";

		boolean alreadyPrinting = false;

		if (weeks > 0) {
			output += String.format("%2dw ", weeks);
			alreadyPrinting = true;
		}
		else if (highestTime.isAtLeast(HighestTime.Week)) {
			output += "    ";
		}

		if (days > 0 || alreadyPrinting) {
			output += String.format("%dd ", days);
			alreadyPrinting = true;
		}
		else if (highestTime.isAtLeast(HighestTime.Day)) {
			output += "   ";
		}

		if (hours > 0 || alreadyPrinting) {
			output += String.format("%dh ", hours);
			alreadyPrinting = true;
		}
		else if (highestTime.isAtLeast(HighestTime.Hour)) {
			output += "   ";
		}

		if (minutes > 0 || alreadyPrinting) {
			output += String.format("%2dm ", minutes);
		}
		else if (highestTime.isAtLeast(HighestTime.Minute)) {
			output += "    ";
		}

		output += String.format("%2ds", seconds);

		return output;
	}

	public static HighestTime fromTimestamp(long time) {
		if (time >= HOURS_IN_WEEK * SECONDS_IN_HOUR) {
			return HighestTime.Week;
		}
		else if (time >= HOURS_IN_DAY * SECONDS_IN_HOUR) {
			return HighestTime.Day;
		}
		else if (time >= SECONDS_IN_HOUR) {
			return HighestTime.Hour;
		}
		else if (time >= SECONDS_IN_MINUTE) {
			return HighestTime.Minute;
		}
		return HighestTime.Second;
	}

	public static String writeCurrentVersion(OSInterface osInterface) {
		String currentVersion = "Unknown";

		try {
			currentVersion = osInterface.getVersion();
		}
		catch (IOException ignored) {
		}

		try (PrintStream output = new PrintStream(osInterface.createOutputStream("git-data/task-data-version.txt"))) {
			output.print(currentVersion);
		}
		catch (IOException e) {
			e.printStackTrace(System.out);
		}

		return currentVersion;
	}
}
