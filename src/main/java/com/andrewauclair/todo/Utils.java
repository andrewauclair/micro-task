// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

public final class Utils {
	public static final String NL = System.lineSeparator();
	
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
		
		if (weeks > 0 || highestTime.isAtLeast(HighestTime.Week)) {
			output += String.format("%02dw ", weeks);
			highestTime = HighestTime.Week;
		}
		
		if (days > 0 || highestTime.isAtLeast(HighestTime.Day)) {
			output += String.format("%02dd ", days);
			highestTime = HighestTime.Day;
		}
		
		if (hours > 0 || highestTime.isAtLeast(HighestTime.Hour)) {
			output += String.format("%02dh ", hours);
			highestTime = HighestTime.Hour;
		}
		
		if (minutes > 0 || highestTime.isAtLeast(HighestTime.Minute)) {
			output += String.format("%02dm ", minutes);
		}
		
		output += String.format("%02ds", seconds);
		
		return output;
	}
	
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
}
