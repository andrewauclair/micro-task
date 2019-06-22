// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import java.util.Objects;

final class TaskTimes {
	static final long TIME_NOT_SET = Long.MIN_VALUE;

	final long start;
	final long stop;

	TaskTimes(long start, long stop) {
		this.start = start;
		this.stop = stop;
	}

	TaskTimes(long start) {
		this(start, TIME_NOT_SET);
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
	public int hashCode() {
		return Objects.hash(start, stop);
	}

	@Override
	public String toString() {
		if (stop == TIME_NOT_SET) {
			return String.valueOf(start);
		}
		return start + " - " + stop;
	}
}
