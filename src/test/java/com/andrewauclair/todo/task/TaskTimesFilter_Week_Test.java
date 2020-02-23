// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class TaskTimesFilter_Week_Test extends TaskBaseTestCase {
	private final int HOURS_IN_DAY = 86_400;
	
	final long feb19_2020_8_am = 1582117200;
	
	@Test
	void apply_filter_for_week() {
		long time = feb19_2020_8_am;
//		osInterface.setTime(time);
		
		Task task1 = addTaskWithTimes("Test 1", time, time + 1200);
		time += HOURS_IN_DAY;
		
		Task task2 = addTaskWithTimes("Test 2", time, time + 2400);
		time += HOURS_IN_DAY;
		
		Task task3 = addTaskWithTimes("Test 3", time, time + 3600);
		time += HOURS_IN_DAY;
		
		Task task4 = addTaskWithTimes("Test 4", time, time + 4800);
		time += HOURS_IN_DAY;
		
		Task task5 = addTaskWithTimes("Test 5", time, time + 6000);
		time += HOURS_IN_DAY;
		
		Task task6 = addTaskWithTimes("Test 6", time, time + 7200);
		time += HOURS_IN_DAY;
		
		Task task7 = addTaskWithTimes("Test 7", time, time + 8400);
		time += HOURS_IN_DAY;
		
		Task task8 = addTaskWithTimes("Test 8", time, time + 9600);
		time += HOURS_IN_DAY;
		
		Task task9 = addTaskWithTimes("Test 9", time, time + 10800);
		time += HOURS_IN_DAY;
		
		Task task10 = addTaskWithTimes("Test 10", time, time + 12000);
		time += HOURS_IN_DAY;
		
		Task task11 = addTaskWithTimes("Test 11", time, time + 13200);
		time += HOURS_IN_DAY;
		
		Task task12 = addTaskWithTimes("Test 12", time, time + 15400);
		time += HOURS_IN_DAY;
		
		Task task13 = addTaskWithTimes("Test 13", time, time + 16600);
		time += HOURS_IN_DAY;
		
		Task task14 = addTaskWithTimes("Test 14", time, time + 17800);
		time += HOURS_IN_DAY;
		
		Mockito.when(osInterface.getZoneId()).thenReturn(ZoneId.of("America/Chicago"));
		
		TaskTimesFilter filter = new TaskTimesFilter(tasks);
		
		filter = filter.filterForWeek(2, 25, 2020);
		
		assertThat(filter.getData()).containsOnly(
				new TaskTimesFilter.TaskTimeFilterResult(6000, task5, "/default"),
				new TaskTimesFilter.TaskTimeFilterResult(7200, task6, "/default"),
				new TaskTimesFilter.TaskTimeFilterResult(8400, task7, "/default"),
				new TaskTimesFilter.TaskTimeFilterResult(9600, task8, "/default"),
				new TaskTimesFilter.TaskTimeFilterResult(10800, task9, "/default"),
				new TaskTimesFilter.TaskTimeFilterResult(12000, task10, "/default"),
				new TaskTimesFilter.TaskTimeFilterResult(13200, task11, "/default")
		);
	}
	
	Task addTaskWithTimes(String name, long start, long stop) {
		Task task = tasks.addTask(name);
		osInterface.setTime(start);
		tasks.startTask(task.id, false);
		osInterface.setTime(stop);
		return tasks.stopTask();
	}
}
