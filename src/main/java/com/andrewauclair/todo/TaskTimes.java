// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

final class TaskTimes {
	static final class Times {
		private static final long TIME_NOT_SET = Long.MIN_VALUE;
		
		final long start;
		final long stop;
		
		Times(long start, long stop) {
			this.start = start;
			this.stop = stop;
		}
		
		Times(long start) {
			this(start, TIME_NOT_SET);
		}
		
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Times times = (Times) o;
			return start == times.start &&
					stop == times.stop;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(start, stop);
		}
		
		@Override
		public String toString() {
			return start + " - " + stop;
		}
	}
	
	private final List<Times> times;
	
	TaskTimes() {
		times = Collections.emptyList();
	}
	
	TaskTimes(long start, long stop) {
		times = Collections.unmodifiableList(Collections.singletonList(new Times(start, stop)));
	}
	
	TaskTimes(long start) {
		this(start, Times.TIME_NOT_SET);
	}
	
	TaskTimes(List<Times> times) {
		this.times = Collections.unmodifiableList(times);
	}
	
	List<Times> asList() {
		return times;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TaskTimes taskTimes = (TaskTimes) o;
		return Objects.equals(times, taskTimes.times);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(times);
	}
}
