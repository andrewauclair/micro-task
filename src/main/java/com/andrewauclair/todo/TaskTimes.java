// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

final class TaskTimes {
	static final long TIME_NOT_SET = Long.MIN_VALUE;
	
	final long start;
	final long stop;
	
	TaskTimes(long start) {
		this(start, TIME_NOT_SET);
	}
	
	TaskTimes(long start, long stop) {
		this.start = start;
		this.stop = stop;
	}
	
	long getDuration() {
		if (stop == TIME_NOT_SET) {
			return 0;
		}
		return stop - start;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(start, stop);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TaskTimes times = (TaskTimes) o;
		return start == times.start &&
				stop == times.stop;
	}
	
	@Override
	public String toString() {
		if (stop == TIME_NOT_SET) {
			return String.valueOf(start);
		}
		return start + " - " + stop;
	}
	
	String description(ZoneId zone) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
		String startStr = Instant.ofEpochSecond(start).atZone(zone).format(dateTimeFormatter);
		
		if (stop != TIME_NOT_SET) {
			String stopStr = Instant.ofEpochSecond(stop).atZone(zone).format(dateTimeFormatter);
			return startStr + " - " + stopStr;
		}
		return startStr + " -";
	}
}
