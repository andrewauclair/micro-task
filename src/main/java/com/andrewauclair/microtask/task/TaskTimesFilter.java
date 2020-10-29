// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.list.name.ExistingListName;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("CanBeFinal")
public class TaskTimesFilter {
	private final OSInterface osInterface;
	private final Tasks tasks;
	private final List<String> lists = new ArrayList<>();
	private List<Task> allTasks = new ArrayList<>();
	private List<TaskTimeFilterResult> results = new ArrayList<>();

	public TaskTimesFilter(Tasks tasks) {
		osInterface = tasks.osInterface;
		this.tasks = tasks;
		this.allTasks.addAll(tasks.getAllTasks());

		buildResults();
	}

	private void buildResults() {
		results.clear();

		for (Task task : allTasks) {
			long totalTime = 0;

			for (TaskTimes time : task.startStopTimes) {
				totalTime += time.getDuration(osInterface);
			}

			results.add(new TaskTimeFilterResult(totalTime, task, tasks.getListForTask(new ExistingID(tasks, task.id)).getFullPath()));
		}
	}

	public void filterForGroup(TaskGroup group) {
		group.getChildren().stream()
				.filter(child -> child instanceof TaskList)
				.forEach(list -> filterForList(list.getFullPath()));
	}

	public void filterForList(String list) {
		lists.add(list);

		allTasks.clear();
		lists.forEach(name -> allTasks.addAll(tasks.getTasksForList(new ExistingListName(tasks, name))));

		buildResults();
	}

	public TaskTimesFilter filterForDay(int month, int day, int year) {
		LocalDate of = LocalDate.of(year, month, day);

		ZoneId zoneId = osInterface.getZoneId();
		Instant instant = of.atStartOfDay(zoneId).toInstant();

		LocalDate today = LocalDate.ofInstant(instant, zoneId);
		LocalDateTime midnight = LocalDateTime.of(today, LocalTime.MIDNIGHT);
		LocalDateTime nextMidnight = midnight.plusDays(1);

		applyDateRange(zoneId, midnight, nextMidnight);

		return this;
	}

	private void applyDateRange(ZoneId zoneId, LocalDateTime midnight, LocalDateTime nextMidnight) {
		long midnightStart = midnight.atZone(zoneId).toEpochSecond();
		long midnightStop = nextMidnight.atZone(zoneId).toEpochSecond();

		List<Task> newTasks = new ArrayList<>();
		List<TaskTimeFilterResult> newResults = new ArrayList<>();

		for (Task task : allTasks) {
			long totalTime = 0;

			for (TaskTimes time : task.startStopTimes) {
				if (time.start >= midnightStart && time.stop < midnightStop && time.start < midnightStop) {
					totalTime += time.getDuration(osInterface);
				}
			}

			if (totalTime > 0) {
				newTasks.add(task);
				newResults.add(new TaskTimeFilterResult(totalTime, task, tasks.getListForTask(new ExistingID(tasks, task.id)).getFullPath()));
			}
		}

		allTasks = newTasks;
		results = newResults;
	}

	public TaskTimesFilter filterForWeek(int month, int day, int year) {
		LocalDate of = LocalDate.of(year, month, day);

		LocalDate weekStart = of.minusDays(of.getDayOfWeek().getValue());

		ZoneId zoneId = osInterface.getZoneId();

		LocalDateTime midnight = LocalDateTime.of(weekStart, LocalTime.MIDNIGHT);
		LocalDateTime nextMidnight = midnight.plusDays(7);

		applyDateRange(zoneId, midnight, nextMidnight);

		return this;
	}

	public TaskTimesFilter filterForMonth(int month) {
		Instant instant = Instant.ofEpochSecond(osInterface.currentSeconds());

		ZoneId zoneId = osInterface.getZoneId();

		LocalDate currentDate = LocalDate.ofInstant(instant, zoneId);

		int year = currentDate.getYear();

		LocalDate monthStart = LocalDate.of(year, month, 1);

		LocalDateTime midnight = LocalDateTime.of(monthStart, LocalTime.MIDNIGHT);
		LocalDateTime lastMidnight = midnight.plusDays(YearMonth.of(year, month).lengthOfMonth());

		applyDateRange(zoneId, midnight, lastMidnight);

		return this;
	}

	public List<Task> getTasks() {
		return allTasks;
	}

	public List<TaskTimeFilterResult> getData() {
		return results;
	}

	public static final class TaskTimeFilterResult {
		public final long total;
		public final Task task;
		public final String list;

		public TaskTimeFilterResult(long total, Task task, String list) {
			this.total = total;
			this.task = task;
			this.list = list;
		}

		public long getTotal() {
			return total;
		}

		@Override
		public int hashCode() {
			return Objects.hash(total, task, list);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			TaskTimeFilterResult that = (TaskTimeFilterResult) o;
			return total == that.total &&
					Objects.equals(task, that.task) &&
					Objects.equals(list, that.list);
		}

		@Override
		public String toString() {
			return "TaskFilterResult{" +
					"total=" + total +
					", task=" + task +
					", list='" + list + "'" +
					'}';
		}
	}
}
