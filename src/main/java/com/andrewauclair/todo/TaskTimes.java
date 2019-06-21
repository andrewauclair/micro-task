// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

final class TaskTimes {
	static final class Times {
		final int start;
		final int stop;
		
		Times(int start, int stop) {
			this.start = start;
			this.stop = stop;
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
	
	TaskTimes(int start, int stop) {
		times = Collections.unmodifiableList(Collections.singletonList(new Times(start, stop)));
	}
	
	TaskTimes(List<Times> times) {
		this.times = Collections.unmodifiableList(times);
	}
	
	List<Times> asList() {
		return times;
	}
}
