// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskState;
import com.andrewauclair.microtask.task.TaskTimes;
import com.andrewauclair.microtask.task.TaskTimesFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_REVERSED;
import static org.mockito.Mockito.*;

class Commands_Times_Projects_Test extends Commands_Times_BaseTestCase {
	@Test
	void times_with_only_proj_feat_is_invalid() {
		commands.execute(printStream, "times --proj-feat");

		assertOutput(
				"Invalid command.",
				""
		);
	}

	@Test
	void basic_times_command_for_projects_and_features() {
		List<TaskTimes> addTime = Collections.singletonList(new TaskTimes(0));

		Task task1 = new Task(1, "Test 1", TaskState.Active, addTime);
		Task task2 = new Task(2, "Test 2", TaskState.Inactive, addTime);
		Task task3 = new Task(3, "Test 3", TaskState.Finished, addTime);
		Task task5 = new Task(5, "Test 5", TaskState.Inactive, addTime, true);

		tasks.setProject(existingGroup("/"), "Longer Project Name", true);
		tasks.setFeature(existingGroup("/"), "Short Feat", true);

		tasks.setActiveList(existingList("/default"));
		tasks.addTask(task1);
		tasks.addTask(task2);

		tasks.addList(newList("/one"), true);
		tasks.setActiveList(existingList("/one"));
		tasks.setProject(existingList("/one"), "Short Proj", true);
		tasks.setFeature(existingList("/one"), "Longer Feature Name", true);

		tasks.addTask(task3);
		tasks.addTask(task5);

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, task1, "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(21699, task2, "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, task3, "/one"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, task5, "/one")
				)
		);

		commands.execute(printStream, "times --proj-feat --all-time");

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, atLeast(1)).getData();
		order.verifyNoMoreInteractions();

		assertOutput(
				"Time            Project               Feature",
				"",
				"   6h 12m  0s   Longer Project Name   Short Feat",
				"   2h 21m 35s   Short Proj            Short Feat Longer Feature Name",
				"",
				"1d 0h 33m 35s   Total",
				""
		);
	}

	@Test
	void feature_names_when_parent_group_has_no_feature() {
		List<TaskTimes> addTime = Collections.singletonList(new TaskTimes(0));

		Task task1 = new Task(1, "Test 1", TaskState.Active, addTime);
		Task task2 = new Task(2, "Test 2", TaskState.Inactive, addTime);
		Task task3 = new Task(3, "Test 3", TaskState.Finished, addTime);
		Task task5 = new Task(5, "Test 5", TaskState.Inactive, addTime, true);
		Task task6 = new Task(6, "Test 5", TaskState.Inactive, addTime);

		tasks.setProject(existingGroup("/"), "Longer Project Name", true);

		tasks.setActiveList(existingList("/default"));
		tasks.setProject(existingList("/default"), "Short Proj", true);
		tasks.setFeature(existingList("/default"), "UI", true);

		tasks.addTask(task1);
		tasks.addTask(task2);

		tasks.addList(newList("/one"), true);
		tasks.setActiveList(existingList("/one"));
		tasks.setProject(existingList("/one"), "Short Proj", true);
		tasks.setFeature(existingList("/one"), "Longer Feature Name", true);

		tasks.addTask(task3);
		tasks.addTask(task5);

		tasks.addGroup(newGroup("/two/"));
		tasks.addList(newList("/two/three"), true);
		tasks.setActiveList(existingList("/two/three"));
		tasks.setProject(existingList("/two/three"), "Three Proj", true);

		tasks.addTask(task6);

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, task1, "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(21699, task2, "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, task3, "/one"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, task5, "/one"),
						new TaskTimesFilter.TaskTimeFilterResult(5000, task6, "/two/three")
				)
		);

		commands.execute(printStream, "times --proj-feat --all-time");

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, atLeast(1)).getData();
		order.verifyNoMoreInteractions();

		assertOutput(
				"Time            Project      Feature",
				"",
				"   6h 12m  0s   Short Proj   UI",
				"   2h 21m 35s   Short Proj   Longer Feature Name",
				"   1h 23m 20s   Three Proj   " + ANSI_REVERSED + "None" + ANSI_RESET,
				"",
				"1d 1h 56m 55s   Total",
				""
		);
	}

	@Test
	void features_are_inherited_from_parent() {
		List<TaskTimes> addTime = Collections.singletonList(new TaskTimes(0));

		Task task1 = new Task(1, "Test 1", TaskState.Active, addTime);
		Task task2 = new Task(2, "Test 2", TaskState.Inactive, addTime);
		Task task3 = new Task(3, "Test 3", TaskState.Finished, addTime);
		Task task5 = new Task(5, "Test 5", TaskState.Inactive, addTime, true);

		tasks.setProject(existingGroup("/"), "Longer Project Name", true);
		tasks.setFeature(existingGroup("/"), "Impl", true);

		tasks.setActiveList(existingList("/default"));
		tasks.addTask(task1);
		tasks.addTask(task2);

		tasks.addList(newList("/one"), true);
		tasks.setActiveList(existingList("/one"));

		tasks.addTask(task3);
		tasks.addTask(task5);

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, task1, "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(21699, task2, "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, task3, "/one"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, task5, "/one")
				)
		);

		commands.execute(printStream, "times --proj-feat --all-time");

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, atLeast(1)).getData();
		order.verifyNoMoreInteractions();

		assertOutput(
				"Time            Project               Feature",
				"",
				"1d 0h 33m 35s   Longer Project Name   Impl",
				"",
				"1d 0h 33m 35s   Total",
				""
		);
	}

	@Test
	void project_feature_times_today() {
		List<TaskTimes> addTime = Collections.singletonList(new TaskTimes(0));

		Task task1 = new Task(1, "Test 1", TaskState.Active, addTime);
		Task task2 = new Task(2, "Test 2", TaskState.Inactive, addTime);
		Task task3 = new Task(3, "Test 3", TaskState.Finished, addTime);
		Task task5 = new Task(5, "Test 5", TaskState.Inactive, addTime, true);

		tasks.setProject(existingGroup("/"), "Project 1", true);
		tasks.setFeature(existingGroup("/"), "Feature 1", true);

		tasks.setActiveList(existingList("/default"));
		tasks.addTask(task1);
		tasks.addTask(task2);

		tasks.addList(newList("/one"), true);
		tasks.setActiveList(existingList("/one"));
		tasks.setProject(existingList("/one"), "Project 2", true);
		tasks.setFeature(existingList("/one"), "Feature 2", true);

		tasks.addTask(task3);
		tasks.addTask(task5);

		when(mockTaskTimesFilter.getTasks()).thenReturn(
				Arrays.asList(task1, task2, task3, task5)
		);

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, task1, "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, task2, "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, task3, "/one"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, task5, "/one")
				)
		);

		setTime(june17_8_am);

		commands.execute(printStream, "times --proj-feat --today");

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForDay(6, 17, 2019);

		assertOutput(
				"Time         Project     Feature",
				"",
				"2h 21m 35s   Project 2   Feature 1 Feature 2",
				"1h 12m  0s   Project 1   Feature 1",
				"",
				"3h 33m 35s   Total",
				""
		);
	}

	@Test
	void project_feature_times_yesterday() {
		List<TaskTimes> addTime = Collections.singletonList(new TaskTimes(0));

		Task task1 = new Task(1, "Test 1", TaskState.Active, addTime);
		Task task2 = new Task(2, "Test 2", TaskState.Inactive, addTime);
		Task task3 = new Task(3, "Test 3", TaskState.Finished, addTime);
		Task task5 = new Task(5, "Test 5", TaskState.Inactive, addTime, true);

		tasks.setProject(existingGroup("/"), "Project 1", true);
		tasks.setFeature(existingGroup("/"), "Feature 1", true);

		tasks.setActiveList(existingList("/default"));
		tasks.addTask(task1);
		tasks.addTask(task2);

		tasks.addList(newList("/one"), true);
		tasks.setActiveList(existingList("/one"));
		tasks.setProject(existingList("/one"), "Project 2", true);
		tasks.setFeature(existingList("/one"), "Feature 2", true);

		tasks.addTask(task3);
		tasks.addTask(task5);

		when(mockTaskTimesFilter.getTasks()).thenReturn(
				Arrays.asList(task1, task2, task3, task5)
		);

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, task1, "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, task2, "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, task3, "/one"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, task5, "/one")
				)
		);

		setTime(june17_8_am);

		commands.execute(printStream, "times --proj-feat --yesterday");

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForDay(6, 16, 2019);

		assertOutput(
				"Time         Project     Feature",
				"",
				"2h 21m 35s   Project 2   Feature 1 Feature 2",
				"1h 12m  0s   Project 1   Feature 1",
				"",
				"3h 33m 35s   Total",
				""
		);
	}

	@ParameterizedTest
	@ValueSource(strings = {"-m 6 -d 17 -y 2019", "-m 6 -d 17", "-d 17 -y 2019"})
	void basic_times_for_the_day__only_uses_times_from_given_day__midnight_to_midnight(String parameters) {
		setTime(june18_8_am);

		List<TaskTimes> addTime = Collections.singletonList(new TaskTimes(0));

		Task task1 = new Task(1, "Test 1", TaskState.Active, addTime);
		Task task2 = new Task(2, "Test 2", TaskState.Inactive, addTime);
		Task task3 = new Task(3, "Test 3", TaskState.Finished, addTime);
		Task task5 = new Task(5, "Test 5", TaskState.Inactive, addTime, true);

		tasks.setProject(existingGroup("/"), "Project 1", true);
		tasks.setFeature(existingGroup("/"), "Feature 1", true);

		tasks.setActiveList(existingList("/default"));
		tasks.addTask(task1);
		tasks.addTask(task2);

		tasks.addList(newList("/one"), true);
		tasks.setActiveList(existingList("/one"));
		tasks.setProject(existingList("/one"), "Project 2", true);
		tasks.setFeature(existingList("/one"), "Feature 2", true);

		tasks.addTask(task3);
		tasks.addTask(task5);

		when(mockTaskTimesFilter.getTasks()).thenReturn(
				Arrays.asList(task1, task2, task3, task5)
		);

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, task1, "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, task2, "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, task3, "/one"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, task5, "/one")
				)
		);

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, new Task(1, "Test 1", TaskState.Active, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, new Task(2, "Test 2", TaskState.Inactive, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, new Task(3, "Test 3", TaskState.Finished, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, new Task(5, "Test 5", TaskState.Inactive, addTime, true), "/default")
				)
		);

		commands.execute(printStream, "times --proj-feat " + parameters);

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForDay(6, 17, 2019);

		assertOutput(
				"Time         Project     Feature",
				"",
				"2h 21m 35s   Project 2   Feature 1 Feature 2",
				"1h 12m  0s   Project 1   Feature 1",
				"",
				"3h 33m 35s   Total",
				""
		);
	}

	@Test
	void project_feature_output_for_entire_week() {
		setTime(june17_8_am);

		List<TaskTimes> addTime = Collections.singletonList(new TaskTimes(0));

		Task task1 = new Task(1, "Test 1", TaskState.Active, addTime);
		Task task2 = new Task(2, "Test 2", TaskState.Inactive, addTime);
		Task task3 = new Task(3, "Test 3", TaskState.Finished, addTime);
		Task task5 = new Task(5, "Test 5", TaskState.Inactive, addTime, true);

		tasks.setProject(existingGroup("/"), "Project 1", true);
		tasks.setFeature(existingGroup("/"), "Feature 1", true);

		tasks.setActiveList(existingList("/default"));
		tasks.addTask(task1);
		tasks.addTask(task2);

		tasks.addList(newList("/one"), true);
		tasks.setActiveList(existingList("/one"));
		tasks.setProject(existingList("/one"), "Project 2", true);
		tasks.setFeature(existingList("/one"), "Feature 2", true);

		tasks.addTask(task3);
		tasks.addTask(task5);

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, task1, "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, task2, "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, task3, "/one"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, task5, "/one")
				)
		);

		commands.execute(printStream, "times --proj-feat --week");

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForWeek(6, 17, 2019);

		assertOutput(
				"Time         Project     Feature",
				"",
				"2h 21m 35s   Project 2   Feature 1 Feature 2",
				"1h 12m  0s   Project 1   Feature 1",
				"",
				"3h 33m 35s   Total",
				""
		);
	}

	@Test
	void project_feature_output_for_entire_month() {
		setTime(june17_8_am);

		List<TaskTimes> addTime = Collections.singletonList(new TaskTimes(0));

		Task task1 = new Task(1, "Test 1", TaskState.Active, addTime);
		Task task2 = new Task(2, "Test 2", TaskState.Inactive, addTime);
		Task task3 = new Task(3, "Test 3", TaskState.Finished, addTime);
		Task task5 = new Task(5, "Test 5", TaskState.Inactive, addTime, true);

		tasks.setProject(existingGroup("/"), "Project 1", true);
		tasks.setFeature(existingGroup("/"), "Feature 1", true);

		tasks.setActiveList(existingList("/default"));
		tasks.addTask(task1);
		tasks.addTask(task2);

		tasks.addList(newList("/one"), true);
		tasks.setActiveList(existingList("/one"));
		tasks.setProject(existingList("/one"), "Project 2", true);
		tasks.setFeature(existingList("/one"), "Feature 2", true);

		tasks.addTask(task3);
		tasks.addTask(task5);

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, task1, "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, task2, "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, task3, "/one"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, task5, "/one")
				)
		);

		commands.execute(printStream, "times --proj-feat --all-month");

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForMonth(6);

		assertOutput(
				"Time         Project     Feature",
				"",
				"2h 21m 35s   Project 2   Feature 1 Feature 2",
				"1h 12m  0s   Project 1   Feature 1",
				"",
				"3h 33m 35s   Total",
				""
		);
	}

	@Test
	void times_projects_for_a_previous_time_week() {
		setTime(june18_8_am);

		List<TaskTimes> addTime = Collections.singletonList(new TaskTimes(0));

		Task task1 = new Task(1, "Test 1", TaskState.Active, addTime);
		Task task2 = new Task(2, "Test 2", TaskState.Inactive, addTime);
		Task task3 = new Task(3, "Test 3", TaskState.Finished, addTime);
		Task task5 = new Task(5, "Test 5", TaskState.Inactive, addTime, true);

		tasks.setProject(existingGroup("/"), "Project 1", true);
		tasks.setFeature(existingGroup("/"), "Feature 1", true);

		tasks.setActiveList(existingList("/default"));
		tasks.addTask(task1);
		tasks.addTask(task2);

		tasks.addList(newList("/one"), true);
		tasks.setActiveList(existingList("/one"));
		tasks.setProject(existingList("/one"), "Project 2", true);
		tasks.setFeature(existingList("/one"), "Feature 2", true);

		tasks.addTask(task3);
		tasks.addTask(task5);

		when(mockTaskTimesFilter.getTasks()).thenReturn(
				Arrays.asList(task1, task2, task3, task5)
		);

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, task1, "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, task2, "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, task3, "/one"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, task5, "/one")
				)
		);

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, new Task(1, "Test 1", TaskState.Active, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, new Task(2, "Test 2", TaskState.Inactive, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, new Task(3, "Test 3", TaskState.Finished, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, new Task(5, "Test 5", TaskState.Inactive, addTime, true), "/default")
				)
		);

		commands.execute(printStream, "times --proj-feat --week -m 6 -d 17 -y 2019");

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForWeek(6, 17, 2019);

		assertOutput(
				"Time         Project     Feature",
				"",
				"2h 21m 35s   Project 2   Feature 1 Feature 2",
				"1h 12m  0s   Project 1   Feature 1",
				"",
				"3h 33m 35s   Total",
				""
		);
	}

	@Test
	void tasks_with_no_project_or_feature_say_none() {
		List<TaskTimes> addTime = Collections.singletonList(new TaskTimes(0));

		tasks.setActiveList(existingList("/default"));

		tasks.addTask("Test 1");
		addTaskTimes(1, 1561080202, 1561081202);
		Task task1 = new Task(1, "Test 1", TaskState.Active, addTime);

		when(mockTaskTimesFilter.getData()).thenReturn(
				Collections.singletonList(
						new TaskTimesFilter.TaskTimeFilterResult(621, task1, "/default")
				)
		);

		commands.execute(printStream, "times --proj-feat --all-time");

		assertOutput(
				"Time      Project   Feature",
				"",
				"10m 21s   " + ANSI_REVERSED + "None" + ANSI_RESET + "   " + ANSI_REVERSED + "None" + ANSI_RESET,
				"",
				"10m 21s   Total",
				""
		);
	}

	@Test
	void task_with_no_project_says_none() {
		List<TaskTimes> addTime = Collections.singletonList(new TaskTimes(0));

		Task task1 = new Task(1, "Test 1", TaskState.Active, addTime);
		Task task2 = new Task(2, "Test 2", TaskState.Inactive, addTime);
		Task task3 = new Task(3, "Test 3", TaskState.Finished, addTime);
		Task task5 = new Task(5, "Test 5", TaskState.Inactive, addTime, true);

		tasks.setFeature(existingGroup("/"), "Feature 1", true);

		tasks.setActiveList(existingList("/default"));
		tasks.addTask(task1);
		tasks.addTask(task2);

		tasks.addList(newList("/one"), true);
		tasks.setActiveList(existingList("/one"));

		tasks.addTask(task3);
		tasks.addTask(task5);

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, task1, "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, task2, "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, task3, "/one"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, task5, "/one")
				)
		);

		commands.execute(printStream, "times --proj-feat --all-time");

		assertOutput(
				"Time         Project   Feature",
				"",
				"3h 33m 35s   " + ANSI_REVERSED + "None" + ANSI_RESET + "   Feature 1",
				"",
				"3h 33m 35s   Total",
				""
		);
	}

	@Test
	void task_with_no_feature_says_none() {
		List<TaskTimes> addTime = Collections.singletonList(new TaskTimes(0));

		Task task1 = new Task(1, "Test 1", TaskState.Active, addTime);
		Task task2 = new Task(2, "Test 2", TaskState.Inactive, addTime);
		Task task3 = new Task(3, "Test 3", TaskState.Finished, addTime);
		Task task5 = new Task(5, "Test 5", TaskState.Inactive, addTime, true);

		tasks.setProject(existingGroup("/"), "Project 1", true);

		tasks.setActiveList(existingList("/default"));
		tasks.addTask(task1);
		tasks.addTask(task2);

		tasks.addList(newList("/one"), true);
		tasks.setActiveList(existingList("/one"));

		tasks.addTask(task3);
		tasks.addTask(task5);

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, task1, "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, task2, "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, task3, "/one"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, task5, "/one")
				)
		);

		commands.execute(printStream, "times --proj-feat --all-time");

		assertOutput(
				"Time         Project     Feature",
				"",
				"3h 33m 35s" + "   Project 1   " + ANSI_REVERSED + "None" + ANSI_RESET,
				"",
				"3h 33m 35s   Total",
				""
		);
	}

	@Test
	void prints_0_total_when_no_tasks_are_found() {
		tasks.setProject(existingGroup("/"), "Project 1", true);

		when(mockTaskTimesFilter.getData()).thenReturn(Collections.emptyList());

		commands.execute(printStream, "times --proj-feat --all-time");

		assertOutput(
				"Time   Project   Feature",
				"",
				"",
				" 0s   Total",
				""
		);
	}
}
