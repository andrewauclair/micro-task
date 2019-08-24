// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskFilterTest extends TaskBaseTestCase {
	private static final long SECONDS_IN_DAY = 86400;
	private static final long MINUTE = 60;
	private static final long HOUR = 60 * MINUTE;
	
	final long june17_8_am = 1560772800;
	final long june24_8_am = june17_8_am + (7 * SECONDS_IN_DAY);
	
	// TODO Here's a list of what we need to test
	
	// counts, per group(s), per list(s), per project, per feature
	// date range
	
	// TODO Need to build a data set that can thoroughly test all of the above
	// We should have a good break down of all data across lists so that we can make sure we're grabbing the correct data
	// for instance we need to have a task that happens on multiple days so that we can test that we're getting data for a single day
	// data for tasks across weeks, to ensure we're within the date range
	
	
	// can we just repeat the same set of tasks every day for 2 weeks and vary their start/stop times each day?
	// this would greatly simplify how this all works. We can then verify that we get the right data for the date range
	// and we can verify that it's the correct data because the times will be different for each day
	
	// 3 pieces of data per point, 3 tasks in a list, 3 lists in a group? 3 projects, 3 features
	
	// 3 groups - /, /one, /two/three
	// 3 lists - /default, /one/test, /two/three/stuff
	// 9 tasks per list, 3 per feature, per project
	// /default (Test 1 - 9), /one/test (Test 10 - 18), /two/three/stuff (Test 19 - 27)
	// 3 projects, Project 1 (Test 1, 2, 3, 10, 11, 12, 19, 20, 21), Project 2 (Test 4, 5, 6, 13, 14, 15, 22, 23, 24), Project 3 (Test 7, 8, 9, 16, 17, 18, 25, 26, 27)
	// 3 features per project (Feature 1 (1, 10, 19, 4, 13, 22, 7, 16, 25), Feature 2 (2, 11, 20, 5, 14, 23, 8, 17, 26), Feature 3 (3, 12, 21, 6, 15, 24, 9, 18, 27)
	
	// we'll have to have a single active task, can I leave that up to the test that is using the data? It will need to position the active task to be in the right data set
	
	// TODO we're also forgetting about issues here, they aren't all that important right now and I'm not sure I'm going to do any filtering on them, so might be ok leaving them out
	
	// TODO Make sure that a task can't have overlapping times, throw exception somewhere in task, I want to make sure these are all added properly as they will be in the real setup
	
	List<Integer> taskLengths = new ArrayList<>();
	
	//	@BeforeEach
	void setup() {
		int count = 81;
		for (int i = 0; i < 27; i++) {
			taskLengths.add(count);
			count -= 3;
		}
		
		tasks.addTask(new Task(1, "Test 1", TaskState.Inactive, createTaskTimes(1), false, "Project 1", "Feature 1"));
		tasks.addTask(new Task(2, "Test 2", TaskState.Inactive, createTaskTimes(2), false, "Project 1", "Feature 2"));
		tasks.addTask(new Task(3, "Test 3", TaskState.Inactive, createTaskTimes(3), false, "Project 1", "Feature 3"));
		tasks.addTask(new Task(4, "Test 4", TaskState.Inactive, createTaskTimes(4), false, "Project 2", "Feature 1"));
		tasks.addTask(new Task(5, "Test 5", TaskState.Inactive, createTaskTimes(5), false, "Project 2", "Feature 2"));
		tasks.addTask(new Task(6, "Test 6", TaskState.Inactive, createTaskTimes(6), false, "Project 2", "Feature 3"));
		tasks.addTask(new Task(7, "Test 7", TaskState.Inactive, createTaskTimes(7), false, "Project 3", "Feature 1"));
		tasks.addTask(new Task(8, "Test 8", TaskState.Inactive, createTaskTimes(8), false, "Project 3", "Feature 2"));
		tasks.addTask(new Task(9, "Test 9", TaskState.Inactive, createTaskTimes(9), false, "Project 3", "Feature 3"));
		
		tasks.createGroup("/one");
		tasks.switchGroup("/one");
		
		tasks.addList("test");
		tasks.setActiveList("test");
		
		tasks.addTask(new Task(10, "Test 10", TaskState.Inactive, createTaskTimes(10), false, "Project 1", "Feature 1"));
		tasks.addTask(new Task(11, "Test 11", TaskState.Inactive, createTaskTimes(11), false, "Project 1", "Feature 2"));
		tasks.addTask(new Task(12, "Test 12", TaskState.Inactive, createTaskTimes(12), false, "Project 1", "Feature 3"));
		tasks.addTask(new Task(13, "Test 13", TaskState.Inactive, createTaskTimes(13), false, "Project 2", "Feature 1"));
		tasks.addTask(new Task(14, "Test 14", TaskState.Inactive, createTaskTimes(14), false, "Project 2", "Feature 2"));
		tasks.addTask(new Task(15, "Test 15", TaskState.Inactive, createTaskTimes(15), false, "Project 2", "Feature 3"));
		tasks.addTask(new Task(16, "Test 16", TaskState.Inactive, createTaskTimes(16), false, "Project 3", "Feature 1"));
		tasks.addTask(new Task(17, "Test 17", TaskState.Inactive, createTaskTimes(17), false, "Project 3", "Feature 2"));
		tasks.addTask(new Task(18, "Test 18", TaskState.Inactive, createTaskTimes(18), false, "Project 3", "Feature 3"));
		
		tasks.createGroup("/two/three");
		tasks.switchGroup("/two/three");
		
		tasks.addList("stuff");
		tasks.setActiveList("stuff");
		
		tasks.addTask(new Task(19, "Test 19", TaskState.Inactive, createTaskTimes(19), false, "Project 1", "Feature 1"));
		tasks.addTask(new Task(20, "Test 20", TaskState.Inactive, createTaskTimes(20), false, "Project 1", "Feature 2"));
		tasks.addTask(new Task(21, "Test 21", TaskState.Inactive, createTaskTimes(21), false, "Project 1", "Feature 3"));
		tasks.addTask(new Task(22, "Test 22", TaskState.Inactive, createTaskTimes(22), false, "Project 2", "Feature 1"));
		tasks.addTask(new Task(23, "Test 23", TaskState.Inactive, createTaskTimes(23), false, "Project 2", "Feature 2"));
		tasks.addTask(new Task(24, "Test 24", TaskState.Inactive, createTaskTimes(24), false, "Project 2", "Feature 3"));
		tasks.addTask(new Task(25, "Test 25", TaskState.Inactive, createTaskTimes(25), false, "Project 3", "Feature 1"));
		tasks.addTask(new Task(26, "Test 26", TaskState.Inactive, createTaskTimes(26), false, "Project 3", "Feature 2"));
		tasks.addTask(new Task(27, "Test 27", TaskState.Inactive, createTaskTimes(27), false, "Project 3", "Feature 3"));
		
		tasks.addTask(new Task(28, "Test 28", TaskState.Inactive, Collections.singletonList(new TaskTimes(june17_8_am, june17_8_am + 10))));
	}
	
	private List<TaskTimes> createTaskTimes(long id) {
		// june 17 to june 28
		List<TaskTimes> times = new ArrayList<>();
		
		// all tasks will be at the same time every day, length will be a fixed time - id * 1 minute
		// id * 1 minute would be max 12 * 1 minutes, so that might not work
		
		long index = id - 1;
		
		long time = june17_8_am;
		
		// Week 1
		for (int i = 0; i < 5; i++) {
			if (id % 5 != i) {
				long startTime = time + getTaskStartTime(id, index);
				times.add(new TaskTimes(startTime, startTime + getTaskLength((int) index)));
			}
			index++;
			time += SECONDS_IN_DAY;
		}
		
		time = june24_8_am;
		
		// Week 2
		for (int i = 0; i < 5; i++) {
			if (id % 5 != i) {
				long startTime = time + getTaskStartTime(id, index);
				times.add(new TaskTimes(startTime, startTime + getTaskLength((int) index)));
			}
			index++;
			time += SECONDS_IN_DAY;
		}
		return times;
	}
	
	private long getTaskStartTime(long id, long index) {
		long total = 0;
		
		for (int i = 0; i < id - 1; i++) {
			total += getTaskLength((int) index - i - 1);
		}
		
		return total;
	}
	
	private long getTaskLength(int index) {
		if (index < 0) {
			return taskLengths.get(index + taskLengths.size());
		}
		if (index < taskLengths.size()) {
			return taskLengths.get(index);
		}
		return taskLengths.get(index - taskLengths.size());
	}
	
	//	@Test
	void verify_setup_data() {
		// there's so much data here that I want to verify that we've configured it all correctly before trusting it
		assertThat(tasks.getTasksForList("/default")).containsOnly(
				new Task(1, "Test 1", TaskState.Inactive, createTaskTimes(1), false, "Project 1", "Feature 1"),
				new Task(2, "Test 2", TaskState.Inactive, createTaskTimes(2), false, "Project 1", "Feature 2"),
				new Task(3, "Test 3", TaskState.Inactive, createTaskTimes(3), false, "Project 1", "Feature 3"),
				new Task(4, "Test 4", TaskState.Inactive, createTaskTimes(4), false, "Project 2", "Feature 1"),
				new Task(5, "Test 5", TaskState.Inactive, createTaskTimes(5), false, "Project 2", "Feature 2"),
				new Task(6, "Test 6", TaskState.Inactive, createTaskTimes(6), false, "Project 2", "Feature 3"),
				new Task(7, "Test 7", TaskState.Inactive, createTaskTimes(7), false, "Project 3", "Feature 1"),
				new Task(8, "Test 8", TaskState.Inactive, createTaskTimes(8), false, "Project 3", "Feature 2"),
				new Task(9, "Test 9", TaskState.Inactive, createTaskTimes(9), false, "Project 3", "Feature 3")
		);
		
		assertThat(tasks.getTasksForList("/one/test")).containsOnly(
				new Task(10, "Test 10", TaskState.Inactive, createTaskTimes(10), false, "Project 1", "Feature 1"),
				new Task(11, "Test 11", TaskState.Inactive, createTaskTimes(11), false, "Project 1", "Feature 2"),
				new Task(12, "Test 12", TaskState.Inactive, createTaskTimes(12), false, "Project 1", "Feature 3"),
				new Task(13, "Test 13", TaskState.Inactive, createTaskTimes(13), false, "Project 2", "Feature 1"),
				new Task(14, "Test 14", TaskState.Inactive, createTaskTimes(14), false, "Project 2", "Feature 2"),
				new Task(15, "Test 15", TaskState.Inactive, createTaskTimes(15), false, "Project 2", "Feature 3"),
				new Task(16, "Test 16", TaskState.Inactive, createTaskTimes(16), false, "Project 3", "Feature 1"),
				new Task(17, "Test 17", TaskState.Inactive, createTaskTimes(17), false, "Project 3", "Feature 2"),
				new Task(18, "Test 18", TaskState.Inactive, createTaskTimes(18), false, "Project 3", "Feature 3")
		);
		
		assertThat(tasks.getTasksForList("/two/three/stuff")).containsOnly(
				new Task(19, "Test 19", TaskState.Inactive, createTaskTimes(19), false, "Project 1", "Feature 1"),
				new Task(20, "Test 20", TaskState.Inactive, createTaskTimes(20), false, "Project 1", "Feature 2"),
				new Task(21, "Test 21", TaskState.Inactive, createTaskTimes(21), false, "Project 1", "Feature 3"),
				new Task(22, "Test 22", TaskState.Inactive, createTaskTimes(22), false, "Project 2", "Feature 1"),
				new Task(23, "Test 23", TaskState.Inactive, createTaskTimes(23), false, "Project 2", "Feature 2"),
				new Task(24, "Test 24", TaskState.Inactive, createTaskTimes(24), false, "Project 2", "Feature 3"),
				new Task(25, "Test 25", TaskState.Inactive, createTaskTimes(25), false, "Project 3", "Feature 1"),
				new Task(26, "Test 26", TaskState.Inactive, createTaskTimes(26), false, "Project 3", "Feature 2"),
				new Task(27, "Test 27", TaskState.Inactive, createTaskTimes(27), false, "Project 3", "Feature 3")
		);
	}
	
	private TaskTimes createTimes(long start, long length) {
		return new TaskTimes(start, start + length);
	}
	
	@Test
	void TaskFilterResult_equals() {
		EqualsVerifier.forClass(TaskFilter.TaskFilterResult.class).verify();
	}
	
	@Test
	void TaskFilterResult_toString() {
		assertEquals("TaskFilterResult{total=1000, task=Task{id=1, task='Test', state=Inactive, taskTimes=[1000 - 2000], recurring=false, project='', feature=''}}",
				new TaskFilter.TaskFilterResult(1000, new Task(1, "Test", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000, 2000)))).toString());
	}
	
	@Test
	void includes_only_tasks_for_day() {
		tasks.addTask(new Task(1, "Test 1", TaskState.Inactive, Arrays.asList(new TaskTimes(1000), createTimes(june17_8_am, HOUR)), false, "Project 1", "Feature 1"));
		Task task2 = new Task(2, "Test 2", TaskState.Inactive, Arrays.asList(new TaskTimes(1000), createTimes(june17_8_am + SECONDS_IN_DAY, HOUR)), false, "Project 1", "Feature 1");
		tasks.addTask(task2);
		
		Mockito.when(osInterface.getZoneId()).thenReturn(ZoneId.of("America/Chicago"));
		
		TaskFilter filter = new TaskFilter(tasks);
		
		filter.filterForDay(6, 18, 2019);
		
		assertThat(filter.getData()).containsOnly(
				new TaskFilter.TaskFilterResult(HOUR, task2)
		);
	}
	
	@Test
	void includes_only_times_from_correct_day() {
		tasks.addTask(new Task(1, "Test 1", TaskState.Inactive, Arrays.asList(new TaskTimes(1000), createTimes(june17_8_am, HOUR)), false, "Project 1", "Feature 1"));
		Task task2 = new Task(2, "Test 2", TaskState.Inactive, Arrays.asList(new TaskTimes(1000), createTimes(june17_8_am + SECONDS_IN_DAY, HOUR), createTimes(june24_8_am, HOUR)), false, "Project 1", "Feature 1");
		tasks.addTask(task2);
		
		Mockito.when(osInterface.getZoneId()).thenReturn(ZoneId.of("America/Chicago"));
		
		TaskFilter filter = new TaskFilter(tasks);
		
		filter.filterForDay(6, 18, 2019);
		
		assertThat(filter.getData()).containsOnly(
				new TaskFilter.TaskFilterResult(HOUR, task2)
		);
	}
	
	@Test
	void ignores_active_task_on_latest_day() {
		tasks.addTask(new Task(1, "Test 1", TaskState.Inactive, Arrays.asList(new TaskTimes(1000), createTimes(june17_8_am, HOUR)), false, "Project 1", "Feature 1"));
		Task task2 = new Task(2, "Test 2", TaskState.Inactive, Arrays.asList(new TaskTimes(1000), createTimes(june17_8_am + SECONDS_IN_DAY, HOUR)), false, "Project 1", "Feature 1");
		tasks.addTask(task2);
		
		tasks.addTask(new Task(3, "Test 3", TaskState.Active, Arrays.asList(new TaskTimes(1000), new TaskTimes(june24_8_am)), false, "", ""));
		
		Mockito.when(osInterface.getZoneId()).thenReturn(ZoneId.of("America/Chicago"));
		
		TaskFilter filter = new TaskFilter(tasks);
		
		filter.filterForDay(6, 18, 2019);
		
		assertThat(filter.getData()).containsOnly(
				new TaskFilter.TaskFilterResult(HOUR, task2)
		);
	}
	
	@Test
	void includes_active_task_using_current_time_as_stop() {
		tasks.addTask(new Task(1, "Test 1", TaskState.Inactive, Arrays.asList(new TaskTimes(1000), createTimes(june17_8_am, HOUR)), false, "Project 1", "Feature 1"));
		Task task2 = new Task(2, "Test 2", TaskState.Inactive, Arrays.asList(new TaskTimes(1000), createTimes(june17_8_am + SECONDS_IN_DAY, HOUR)), false, "Project 1", "Feature 1");
		tasks.addTask(task2);
		
		Task task3 = new Task(3, "Test 3", TaskState.Active, Arrays.asList(new TaskTimes(1000), new TaskTimes(june17_8_am + SECONDS_IN_DAY)), false, "", "");
		tasks.addTask(task3);
		
		Mockito.when(osInterface.currentSeconds()).thenReturn(june17_8_am + SECONDS_IN_DAY + HOUR + MINUTE);
		Mockito.when(osInterface.getZoneId()).thenReturn(ZoneId.of("America/Chicago"));
		
		TaskFilter filter = new TaskFilter(tasks);
		
		filter.filterForDay(6, 18, 2019);
		
		assertThat(filter.getData()).containsOnly(
				new TaskFilter.TaskFilterResult(HOUR, task2),
				new TaskFilter.TaskFilterResult(HOUR + MINUTE, task3)
		);
	}
	
	@Test
	void filter_by_list() throws IOException {
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenReturn(new DataOutputStream(new ByteArrayOutputStream()));
		
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		
		tasks.addList("/test/one");
		tasks.setActiveList("/test/one");
		
		Task task3 = tasks.addTask("Test 3");
		Task task4 = tasks.addTask("Test 4");
		
		tasks.addList("/test/two");
		tasks.setActiveList("/test/two");
		
		tasks.addTask("Test 5");
		
		TaskFilter filter = new TaskFilter(tasks);
		
		filter.filterForList("/test/one");
		
		assertThat(filter.getTasks()).containsOnly(
				task3, task4
		);
	}
}
