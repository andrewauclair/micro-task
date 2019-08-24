// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import com.andrewauclair.todo.os.OSInterface;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TaskFilter {
	private final OSInterface osInterface;
	private final Tasks tasks;
	private List<Task> allTasks = new ArrayList<>();
	private List<TaskFilterResult> results = new ArrayList<>();

	public TaskFilter(Tasks tasks) {
		osInterface = tasks.osInterface;
		this.tasks = tasks;
		this.allTasks.addAll(tasks.getAllTasks());
	}
	
	public TaskFilter filterForList(String list) {
		List<Task> newTasks = new ArrayList<>();
		
		for (Task task : allTasks) {
			if (tasks.findListForTask(task.id).equals(list)) {
				newTasks.add(task);
			}
		}
		
		allTasks = newTasks;
		return this;
	}
	
	public TaskFilter filterForDay(int month, int day, int year) {
		LocalDate of = LocalDate.of(year, month, day);

		ZoneId zoneId = osInterface.getZoneId();
		Instant instant = of.atStartOfDay(zoneId).toInstant();

		LocalDate today = LocalDate.ofInstant(instant, zoneId);
		LocalDateTime midnight = LocalDateTime.of(today, LocalTime.MIDNIGHT);
		LocalDateTime nextMidnight = midnight.plusDays(1);

		long midnightStart = midnight.atZone(zoneId).toEpochSecond();
		long midnightStop = nextMidnight.atZone(zoneId).toEpochSecond();

		List<Task> newTasks = new ArrayList<>();
		List<TaskFilterResult> newResults = new ArrayList<>();
		
		for (Task task : allTasks) {
			long totalTime = 0;

			for (TaskTimes time : task.getStartStopTimes()) {
				if (time.start >= midnightStart && time.stop < midnightStop && time.start < midnightStop) {
					totalTime += time.getDuration(osInterface);
				}
			}

			if (totalTime > 0) {
				newTasks.add(task);
				newResults.add(new TaskFilterResult(totalTime, task));
			}
		}
		allTasks = newTasks;
		results = newResults;

		return this;
	}
	
	public List<Task> getTasks() {
		return allTasks;
	}
	
	public List<TaskFilterResult> getData() {
		return results;
	}

	public static final class TaskFilterResult {
		private final long total;
		private final Task task;

		TaskFilterResult(long total, Task task) {

			this.total = total;
			this.task = task;
		}

		public long getTotal() {
			return total;
		}

		public Task getTask() {
			return task;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			TaskFilterResult that = (TaskFilterResult) o;
			return total == that.total &&
					Objects.equals(task, that.task);
		}

		@Override
		public int hashCode() {
			return Objects.hash(total, task);
		}

		@Override
		public String toString() {
			return "TaskFilterResult{" +
					"total=" + total +
					", task=" + task +
					'}';
		}
	}
}
