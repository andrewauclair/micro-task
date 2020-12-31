// Copyright (C) 2019-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.project.*;
import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskState;
import com.andrewauclair.microtask.task.TaskTimes;
import com.andrewauclair.microtask.task.TaskTimesFilter;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.andrewauclair.microtask.TestUtils.newTask;
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
		long addTime = 0;

		Task task1 = newTask(1, "Test 1", TaskState.Active, addTime);
		Task task2 = newTask(2, "Test 2", TaskState.Inactive, addTime);
		Task task3 = newTask(3, "Test 3", TaskState.Finished, addTime);
		Task task5 = newTask(5, "Test 5", TaskState.Inactive, addTime, true);

		projects.createProject(new NewProject(projects, "project-1"), true);
		projects.createProject(new NewProject(projects, "project-2"), true);

		tasks.addList(newList("/projects/project-1/one"), true);
		tasks.addList(newList("/projects/project-2/two"), true);

		Project project1 = projects.getProject(new ExistingProject(projects, "project-1"));
		project1.addFeature(new NewFeature(project1, "one"), true);

		project1.getFeature(new ExistingFeature(project1, "one")).addList(new ExistingListName(tasks, "/projects/project-1/one"));

		Project project2 = projects.getProject(new ExistingProject(projects, "project-2"));
		project2.addFeature(new NewFeature(project2, "two"), true);

		project2.getFeature(new ExistingFeature(project2, "two")).addList(new ExistingListName(tasks, "/projects/project-2/two"));

		tasks.setCurrentList(existingList("/projects/project-1/one"));
		tasks.addTask(task1);
		tasks.addTask(task2);

		tasks.setCurrentList(existingList("/projects/project-2/two"));
		tasks.addTask(task3);
		tasks.addTask(task5);

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, task1, "/projects/projects-1/one"),
						new TaskTimesFilter.TaskTimeFilterResult(21699, task2, "/projects/projects-1/one"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, task3, "/projects/projects-2/two"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, task5, "/projects/projects-2/two")
				)
		);

		commands.execute(printStream, "times --proj-feat --all-time");

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, atLeast(1)).getData();
		order.verifyNoMoreInteractions();

		assertOutput(
				"Time            Project     Feature",
				"",
				"   6h 12m  0s   project-1   one",
				"   2h 21m 35s   project-2   two",
				"",
				"1d 0h 33m 35s   Total",
				""
		);
	}

	@Test
	void feature_names_when_parent_group_has_no_feature() {
		long addTime = 0;

		Task task1 = newTask(1, "Test 1", TaskState.Active, addTime);
		Task task2 = newTask(2, "Test 2", TaskState.Inactive, addTime);
		Task task3 = newTask(3, "Test 3", TaskState.Finished, addTime);
		Task task5 = newTask(5, "Test 5", TaskState.Inactive, addTime, true);
		Task task6 = newTask(6, "Test 5", TaskState.Inactive, addTime);

		projects.createProject(new NewProject(projects, "project-1"), true);

		tasks.addList(newList("/projects/project-1/one"), true);
		tasks.addList(newList("/projects/project-1/two"), true);
		tasks.addList(newList("/projects/project-1/meetings"), true);

		Project project1 = projects.getProject(new ExistingProject(projects, "project-1"));
		project1.addFeature(new NewFeature(project1, "one"), true);

		project1.getFeature(new ExistingFeature(project1, "one")).addList(new ExistingListName(tasks, "/projects/project-1/one"));

		project1.addFeature(new NewFeature(project1, "two"), true);

		project1.getFeature(new ExistingFeature(project1, "two")).addList(new ExistingListName(tasks, "/projects/project-1/two"));

		tasks.setCurrentList(existingList("/projects/project-1/one"));
		tasks.addTask(task1);
		tasks.addTask(task2);

		tasks.setCurrentList(existingList("/projects/project-1/two"));
		tasks.addTask(task3);
		tasks.addTask(task5);

		tasks.setCurrentList(existingList("/projects/project-1/meetings"));

		tasks.addTask(task6);

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, task1, "/projects/project-1/one"),
						new TaskTimesFilter.TaskTimeFilterResult(21699, task2, "/projects/project-1/one"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, task3, "/projects/project-1/two"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, task5, "/projects/project-1/two"),
						new TaskTimesFilter.TaskTimeFilterResult(5000, task6, "/meetings")
				)
		);

		commands.execute(printStream, "times --proj-feat --all-time");

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, atLeast(1)).getData();
		order.verifyNoMoreInteractions();

		assertOutput(
				"Time            Project     Feature",
				"",
				"   6h 12m  0s   project-1   one",
				"   2h 21m 35s   project-1   two",
				"   1h 23m 20s   project-1   " + ANSI_REVERSED + "None" + ANSI_RESET,
				"",
				"1d 1h 56m 55s   Total",
				""
		);
	}

	@Test
	void features_are_inherited_from_parent() {
		long addTime = 0;

		Task task1 = newTask(1, "Test 1", TaskState.Active, addTime);
		Task task2 = newTask(2, "Test 2", TaskState.Inactive, addTime);
		Task task3 = newTask(3, "Test 3", TaskState.Finished, addTime);
		Task task5 = newTask(5, "Test 5", TaskState.Inactive, addTime, true);

		projects.createProject(new NewProject(projects, "project-1"), true);

		tasks.createGroup(newGroup("/projects/project-1/feature-1/"));

		tasks.addList(newList("/projects/project-1/feature-1/one"), true);

		Project project1 = projects.getProject(new ExistingProject(projects, "project-1"));
		project1.addFeature(new NewFeature(project1, "feature-1"), true);

		project1.getFeature(new ExistingFeature(project1, "feature-1")).addGroup(existingGroup("/projects/project-1/feature-1/"));

		tasks.setCurrentList(existingList("/projects/project-1/feature-1/one"));

//		tasks.setProject(existingGroup("/"), "Longer Project Name", true);
//		tasks.setFeature(existingGroup("/"), "Impl", true);

		tasks.setCurrentList(existingList("/projects/project-1/feature-1/one"));
		tasks.addTask(task1);
		tasks.addTask(task2);

//		tasks.addList(newList("/one"), true);
//		tasks.setCurrentList(existingList("/one"));

		tasks.addTask(task3);
		tasks.addTask(task5);

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, task1, "/projects/project-1/feature-1/one"),
						new TaskTimesFilter.TaskTimeFilterResult(21699, task2, "/projects/project-1/feature-1/one"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, task3, "/projects/project-1/feature-1/one"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, task5, "/projects/project-1/feature-1/one")
				)
		);

		commands.execute(printStream, "times --proj-feat --all-time");

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, atLeast(1)).getData();
		order.verifyNoMoreInteractions();

		assertOutput(
				"Time            Project     Feature",
				"",
				"1d 0h 33m 35s   project-1   feature-1",
				"",
				"1d 0h 33m 35s   Total",
				""
		);
	}

	@Test
	void project_feature_times_today() {
		long addTime = 0;

		Task task1 = newTask(1, "Test 1", TaskState.Active, addTime);
		Task task2 = newTask(2, "Test 2", TaskState.Inactive, addTime);
		Task task3 = newTask(3, "Test 3", TaskState.Finished, addTime);
		Task task5 = newTask(5, "Test 5", TaskState.Inactive, addTime, true);

		projects.createProject(new NewProject(projects, "project-1"), true);
		projects.createProject(new NewProject(projects, "project-2"), true);

		tasks.addList(newList("/projects/project-1/one"), true);
		tasks.addList(newList("/projects/project-2/two"), true);

		Project project1 = projects.getProject(new ExistingProject(projects, "project-1"));
		project1.addFeature(new NewFeature(project1, "one"), true);

		project1.getFeature(new ExistingFeature(project1, "one")).addList(new ExistingListName(tasks, "/projects/project-1/one"));

		Project project2 = projects.getProject(new ExistingProject(projects, "project-2"));
		project2.addFeature(new NewFeature(project2, "two"), true);

		project2.getFeature(new ExistingFeature(project2, "two")).addList(new ExistingListName(tasks, "/projects/project-2/two"));

		tasks.setCurrentList(existingList("/projects/project-1/one"));
		tasks.addTask(task1);
		tasks.addTask(task2);

		tasks.setCurrentList(existingList("/projects/project-2/two"));
		tasks.addTask(task3);
		tasks.addTask(task5);

		when(mockTaskTimesFilter.getTasks()).thenReturn(
				Arrays.asList(task1, task2, task3, task5)
		);

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, task1, "/projects/project-1/one"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, task2, "/projects/project-1/one"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, task3, "/projects/project-2/two"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, task5, "/projects/project-2/two")
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
				"2h 21m 35s   project-2   two",
				"1h 12m  0s   project-1   one",
				"",
				"3h 33m 35s   Total",
				""
		);
	}

	@Test
	void project_feature_times_yesterday() {
		long addTime = 0;

		Task task1 = newTask(1, "Test 1", TaskState.Active, addTime);
		Task task2 = newTask(2, "Test 2", TaskState.Inactive, addTime);
		Task task3 = newTask(3, "Test 3", TaskState.Finished, addTime);
		Task task5 = newTask(5, "Test 5", TaskState.Inactive, addTime, true);

		projects.createProject(new NewProject(projects, "project-1"), true);
		projects.createProject(new NewProject(projects, "project-2"), true);

		tasks.addList(newList("/projects/project-1/one"), true);
		tasks.addList(newList("/projects/project-2/two"), true);

		Project project1 = projects.getProject(new ExistingProject(projects, "project-1"));
		project1.addFeature(new NewFeature(project1, "one"), true);

		project1.getFeature(new ExistingFeature(project1, "one")).addList(new ExistingListName(tasks, "/projects/project-1/one"));

		Project project2 = projects.getProject(new ExistingProject(projects, "project-2"));
		project2.addFeature(new NewFeature(project2, "two"), true);

		project2.getFeature(new ExistingFeature(project2, "two")).addList(new ExistingListName(tasks, "/projects/project-2/two"));

		tasks.setCurrentList(existingList("/projects/project-1/one"));
		tasks.addTask(task1);
		tasks.addTask(task2);

		tasks.setCurrentList(existingList("/projects/project-2/two"));
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
				"2h 21m 35s   project-2   two",
				"1h 12m  0s   project-1   one",
				"",
				"3h 33m 35s   Total",
				""
		);
	}

	@ParameterizedTest
	@ValueSource(strings = {"-m 6 -d 17 -y 2019", "-m 6 -d 17", "-d 17 -y 2019"})
	void basic_times_for_the_day__only_uses_times_from_given_day__midnight_to_midnight(String parameters) {
		setTime(june18_8_am);

		long addTime = 0;

		Task task1 = newTask(1, "Test 1", TaskState.Active, addTime);
		Task task2 = newTask(2, "Test 2", TaskState.Inactive, addTime);
		Task task3 = newTask(3, "Test 3", TaskState.Finished, addTime);
		Task task5 = newTask(5, "Test 5", TaskState.Inactive, addTime, true);

		projects.createProject(new NewProject(projects, "project-1"), true);
		projects.createProject(new NewProject(projects, "project-2"), true);

		tasks.addList(newList("/projects/project-1/one"), true);
		tasks.addList(newList("/projects/project-2/two"), true);

		Project project1 = projects.getProject(new ExistingProject(projects, "project-1"));
		project1.addFeature(new NewFeature(project1, "one"), true);

		project1.getFeature(new ExistingFeature(project1, "one")).addList(new ExistingListName(tasks, "/projects/project-1/one"));

		Project project2 = projects.getProject(new ExistingProject(projects, "project-2"));
		project2.addFeature(new NewFeature(project2, "two"), true);

		project2.getFeature(new ExistingFeature(project2, "two")).addList(new ExistingListName(tasks, "/projects/project-2/two"));

		tasks.setCurrentList(existingList("/projects/project-1/one"));
		tasks.addTask(task1);
		tasks.addTask(task2);

		tasks.setCurrentList(existingList("/projects/project-2/two"));
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
						new TaskTimesFilter.TaskTimeFilterResult(621, newTask(1, "Test 1", TaskState.Active, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, newTask(2, "Test 2", TaskState.Inactive, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, newTask(3, "Test 3", TaskState.Finished, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, newTask(5, "Test 5", TaskState.Inactive, addTime, true), "/default")
				)
		);

		commands.execute(printStream, "times --proj-feat " + parameters);

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForDay(6, 17, 2019);

		assertOutput(
				"Time         Project     Feature",
				"",
				"2h 21m 35s   project-2   two",
				"1h 12m  0s   project-1   one",
				"",
				"3h 33m 35s   Total",
				""
		);
	}

	@Test
	void project_feature_output_for_entire_week() {
		setTime(june17_8_am);

		long addTime = 0;

		Task task1 = newTask(1, "Test 1", TaskState.Active, addTime);
		Task task2 = newTask(2, "Test 2", TaskState.Inactive, addTime);
		Task task3 = newTask(3, "Test 3", TaskState.Finished, addTime);
		Task task5 = newTask(5, "Test 5", TaskState.Inactive, addTime, true);

		projects.createProject(new NewProject(projects, "project-1"), true);
		projects.createProject(new NewProject(projects, "project-2"), true);

		tasks.addList(newList("/projects/project-1/one"), true);
		tasks.addList(newList("/projects/project-2/two"), true);

		Project project1 = projects.getProject(new ExistingProject(projects, "project-1"));
		project1.addFeature(new NewFeature(project1, "one"), true);

		project1.getFeature(new ExistingFeature(project1, "one")).addList(new ExistingListName(tasks, "/projects/project-1/one"));

		Project project2 = projects.getProject(new ExistingProject(projects, "project-2"));
		project2.addFeature(new NewFeature(project2, "two"), true);

		project2.getFeature(new ExistingFeature(project2, "two")).addList(new ExistingListName(tasks, "/projects/project-2/two"));

		tasks.setCurrentList(existingList("/projects/project-1/one"));
		tasks.addTask(task1);
		tasks.addTask(task2);

		tasks.setCurrentList(existingList("/projects/project-2/two"));
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
				"2h 21m 35s   project-2   two",
				"1h 12m  0s   project-1   one",
				"",
				"3h 33m 35s   Total",
				""
		);
	}

	@Test
	void project_feature_output_for_entire_month() {
		setTime(june17_8_am);

		long addTime = 0;

		Task task1 = newTask(1, "Test 1", TaskState.Active, addTime);
		Task task2 = newTask(2, "Test 2", TaskState.Inactive, addTime);
		Task task3 = newTask(3, "Test 3", TaskState.Finished, addTime);
		Task task5 = newTask(5, "Test 5", TaskState.Inactive, addTime, true);

		projects.createProject(new NewProject(projects, "project-1"), true);
		projects.createProject(new NewProject(projects, "project-2"), true);

		tasks.addList(newList("/projects/project-1/one"), true);
		tasks.addList(newList("/projects/project-2/two"), true);

		Project project1 = projects.getProject(new ExistingProject(projects, "project-1"));
		project1.addFeature(new NewFeature(project1, "one"), true);

		project1.getFeature(new ExistingFeature(project1, "one")).addList(new ExistingListName(tasks, "/projects/project-1/one"));

		Project project2 = projects.getProject(new ExistingProject(projects, "project-2"));
		project2.addFeature(new NewFeature(project2, "two"), true);

		project2.getFeature(new ExistingFeature(project2, "two")).addList(new ExistingListName(tasks, "/projects/project-2/two"));

		tasks.setCurrentList(existingList("/projects/project-1/one"));
		tasks.addTask(task1);
		tasks.addTask(task2);

		tasks.setCurrentList(existingList("/projects/project-2/two"));
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
				"2h 21m 35s   project-2   two",
				"1h 12m  0s   project-1   one",
				"",
				"3h 33m 35s   Total",
				""
		);
	}

	@Test
	void times_projects_for_a_previous_time_week() {
		setTime(june18_8_am);

		long addTime = 0;

		Task task1 = newTask(1, "Test 1", TaskState.Active, addTime);
		Task task2 = newTask(2, "Test 2", TaskState.Inactive, addTime);
		Task task3 = newTask(3, "Test 3", TaskState.Finished, addTime);
		Task task5 = newTask(5, "Test 5", TaskState.Inactive, addTime, true);

		projects.createProject(new NewProject(projects, "project-1"), true);
		projects.createProject(new NewProject(projects, "project-2"), true);

		tasks.addList(newList("/projects/project-1/one"), true);
		tasks.addList(newList("/projects/project-2/two"), true);

		Project project1 = projects.getProject(new ExistingProject(projects, "project-1"));
		project1.addFeature(new NewFeature(project1, "one"), true);

		project1.getFeature(new ExistingFeature(project1, "one")).addList(new ExistingListName(tasks, "/projects/project-1/one"));

		Project project2 = projects.getProject(new ExistingProject(projects, "project-2"));
		project2.addFeature(new NewFeature(project2, "two"), true);

		project2.getFeature(new ExistingFeature(project2, "two")).addList(new ExistingListName(tasks, "/projects/project-2/two"));

		tasks.setCurrentList(existingList("/projects/project-1/one"));
		tasks.addTask(task1);
		tasks.addTask(task2);

		tasks.setCurrentList(existingList("/projects/project-2/two"));
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
						new TaskTimesFilter.TaskTimeFilterResult(621, newTask(1, "Test 1", TaskState.Active, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, newTask(2, "Test 2", TaskState.Inactive, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, newTask(3, "Test 3", TaskState.Finished, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, newTask(5, "Test 5", TaskState.Inactive, addTime, true), "/default")
				)
		);

		commands.execute(printStream, "times --proj-feat --week -m 6 -d 17 -y 2019");

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForWeek(6, 17, 2019);

		assertOutput(
				"Time         Project     Feature",
				"",
				"2h 21m 35s   project-2   two",
				"1h 12m  0s   project-1   one",
				"",
				"3h 33m 35s   Total",
				""
		);
	}

	@Test
	void tasks_with_no_project_or_feature_say_none() {
		long addTime = 0;

		tasks.setCurrentList(existingList("/default"));

		tasks.addTask("Test 1");
		addTaskTimes(1, 1561080202, 1561081202);
		Task task1 = newTask(1, "Test 1", TaskState.Active, addTime);

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
		long addTime = 0;

		Task task1 = newTask(1, "Test 1", TaskState.Active, addTime);
		Task task2 = newTask(2, "Test 2", TaskState.Inactive, addTime);
		Task task3 = newTask(3, "Test 3", TaskState.Finished, addTime);
		Task task5 = newTask(5, "Test 5", TaskState.Inactive, addTime, true);

//		tasks.setFeature(existingGroup("/"), "Feature 1", true);

		tasks.setCurrentList(existingList("/default"));
		tasks.addTask(task1);
		tasks.addTask(task2);

		tasks.addList(newList("/one"), true);
		tasks.setCurrentList(existingList("/one"));

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
				"3h 33m 35s   " + ANSI_REVERSED + "None" + ANSI_RESET + "   " + ANSI_REVERSED + "None" + ANSI_RESET,
				"",
				"3h 33m 35s   Total",
				""
		);
	}

	@Test
	void prints_0_total_when_no_tasks_are_found() {
//		tasks.setProject(existingGroup("/"), "Project 1", true);

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
