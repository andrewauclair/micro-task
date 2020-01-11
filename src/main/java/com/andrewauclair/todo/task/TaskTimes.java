// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import com.andrewauclair.todo.TaskException;
import com.andrewauclair.todo.os.OSInterface;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public final class TaskTimes {
	static final long TIME_NOT_SET = Long.MIN_VALUE;
	
	public final long start;
	public final long stop;
	public final String project;
	public final String feature;

	public TaskTimes(long start) {
		this(start, TIME_NOT_SET);
	}

	public TaskTimes(long start, String project, String feature) {
		this(start, TIME_NOT_SET, project, feature);
	}

	public TaskTimes(long start, long stop) {
		this(start, stop, "", "");
	}

	public TaskTimes(long start, long stop, String project, String feature) {
		if (stop < start && stop != TIME_NOT_SET) {
			throw new TaskException("Stop time can not come before start time.");
		}

		this.start = start;
		this.stop = stop;
		this.project = project;
		this.feature = feature;
	}
	
	public long getDuration(OSInterface osInterface) {
		if (stop == TIME_NOT_SET) {
			return osInterface.currentSeconds() - start;
		}
		return stop - start;
	}

	@Override
	public int hashCode() {
		return Objects.hash(start, stop, project, feature);
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
				stop == times.stop &&
				Objects.equals(project, times.project) &&
				Objects.equals(feature, times.feature);
	}

	@Override
	public String toString() {
		if (stop == TIME_NOT_SET) {
			return start + ", project='" + project + "', feature='" + feature + "'";
		}
		return start + " - " + stop + ", project='" + project + "', feature='" + feature + "'";
	}
	
	public String description(ZoneId zone) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
		String startStr = Instant.ofEpochSecond(start).atZone(zone).format(dateTimeFormatter);

		if (stop != TIME_NOT_SET) {
			String stopStr = Instant.ofEpochSecond(stop).atZone(zone).format(dateTimeFormatter);
			return startStr + " - " + stopStr;
		}
		return startStr + " -";
	}
}
