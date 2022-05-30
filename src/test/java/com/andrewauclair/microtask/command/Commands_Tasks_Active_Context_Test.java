// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.os.ConsoleColors;
import com.andrewauclair.microtask.project.*;
import com.andrewauclair.microtask.task.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_BOLD;
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_GRAY;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_GREEN;

public class Commands_Tasks_Active_Context_Test extends CommandsBaseTestCase {
	@BeforeEach
	public void setup() throws IOException {
		super.setup();

		tasks.addGroup(newGroup("/projects/micro-task/"));
		tasks.addList(newList("/projects/micro-task/one"), true);
		tasks.addList(newList("/projects/micro-task/two"), true);

		projects.createProject(new NewProject(projects, "micro-task"), true);

		Project project = projects.getProject(new ExistingProject(projects, "micro-task"));

		project.addFeature(new NewFeature(project, "one"), true);
		project.addFeature(new NewFeature(project, "two"), true);
		project.addMilestone(new NewMilestone(project, "20.9.3"), true);

		Milestone milestone = project.getMilestone(new ExistingMilestone(project, "20.9.3"));
		milestone.addFeature(ExistingFeature.tryCreate(project, "two"));

		tasks.setCurrentList(existingList("/projects/micro-task/one"));

		tasks.addTask("Task 1");
		tasks.addTask("Task 2"); // alpha
		tasks.addTask("Task 3"); // bravo
		tasks.addTask("Task 4"); // alpha, bravo

		tasks.setCurrentList(existingList("/projects/micro-task/two"));

		tasks.addTask("Task 5");
		tasks.addTask("Task 6"); // alpha
		tasks.addTask("Task 7"); // bravo
		tasks.addTask("Task 8"); // alpha, bravo

		tasks.setCurrentList(existingList("/default"));
		tasks.addTask("Not displayed");

		tasks.setTags(existingID(2), Collections.singletonList("alpha"));
		tasks.setTags(existingID(3), Collections.singletonList("bravo"));
		tasks.setTags(existingID(4), Arrays.asList("alpha", "bravo"));
		tasks.setTags(existingID(6), Collections.singletonList("alpha"));
		tasks.setTags(existingID(7), Collections.singletonList("bravo"));
		tasks.setTags(existingID(8), Arrays.asList("alpha", "bravo"));
	}

	@Test
	void tasks_on_active_list() {
		tasks.getActiveContext().setActiveList(existingList("/projects/micro-task/one"));

		commands.execute(printStream, "tasks");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Tasks on active list '/projects/micro-task/one'",
				"",
				u + "Type" + r + "  " + u + "ID" + r + "  " + u + r + "     " + u + "Description" + r,
				ANSI_BG_GRAY + "       5  (5)  Task 4     " + ANSI_RESET,
				"       4  (4)  Task 3     ",
				ANSI_BG_GRAY + "       3  (3)  Task 2     " + ANSI_RESET,
				"       2  (2)  Task 1     ",
				"",
				ANSI_BOLD + "Total Tasks: 4" + ANSI_RESET,
				""
		);
	}

	@Test
	void tasks_on_active_list_with_tags() {
		tasks.getActiveContext().setActiveList(existingList("/projects/micro-task/one"));
		tasks.getActiveContext().setActiveTags(Arrays.asList("alpha", "bravo"));

		commands.execute(printStream, "tasks");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Tasks on active list '/projects/micro-task/one' with tag(s): alpha, bravo",
				"",
				u + "Type" + r + "  " + u + "ID" + r + "  " + u + r + "     " + u + "Description" + r,
				ANSI_BG_GRAY + "       4  (4)  Task 3     " + ANSI_RESET,
				"",
				ANSI_BOLD + "Total Tasks: 1" + ANSI_RESET,
				""
		);
	}

	@Test
	void only_active_tags_in_context() {
		tasks.setCurrentList(existingList("/projects/micro-task/one"));
		tasks.getActiveContext().setActiveTags(Arrays.asList("alpha", "bravo"));

		Task active_task = tasks.addTask("Active task");
		tasks.startTask(active_task.ID(), false);

		commands.execute(printStream, "tasks");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Tasks on list '/projects/micro-task/one' with tag(s): alpha, bravo",
				"",
				u + "Type" + r + "  " + u + "ID" + r + "  " + u + r + "      " + u + "Description" + r,
				ANSI_BG_GRAY + "       4   (4)  Task 3     " + ANSI_RESET,
				"",
				"Active Task" + ANSI_RESET,
				ANSI_BG_GREEN + "*     11  (11)  Active task" + ANSI_RESET,
				"",
				ANSI_BOLD + "Total Tasks: 1" + ANSI_RESET,
				""
		);
	}

	@Test
	void tasks_in_active_group() {
		tasks.getActiveContext().setActiveGroup(existingGroup("/projects/micro-task/"));

		commands.execute(printStream, "tasks");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Tasks in active group '/projects/micro-task/'",
				"",
				u + "List" + r + "     " + u + "Type" + r + "  " + u + "ID" + r + "  " + u + r + "     " + u + "Description" + r,
				ANSI_BG_GRAY + "two             9  (9)  Task 8     " + ANSI_RESET,
				"two             8  (8)  Task 7     ",
				ANSI_BG_GRAY + "two             7  (7)  Task 6     " + ANSI_RESET,
				"two             6  (6)  Task 5     ",
				ANSI_BG_GRAY + "one             5  (5)  Task 4     " + ANSI_RESET,
				"one             4  (4)  Task 3     ",
				ANSI_BG_GRAY + "one             3  (3)  Task 2     " + ANSI_RESET,
				"one             2  (2)  Task 1     ",
				ANSI_BG_GRAY + "general   R     1  (1)  Planning   " + ANSI_RESET,
				"",
				ANSI_BOLD + "Total Tasks: 9 (1 Recurring)" + ANSI_RESET,
				""
		);
	}

	@Test
	void tasks_in_active_group_with_tags() {
		tasks.getActiveContext().setActiveGroup(existingGroup("/projects/micro-task/"));
		tasks.getActiveContext().setActiveTags(Arrays.asList("alpha", "bravo"));

		commands.execute(printStream, "tasks");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Tasks in active group '/projects/micro-task/' with tag(s): alpha, bravo",
				"",
				u + "List" + r + "  " + u + "Type" + r + "  " + u + "ID" + r + "  " + u + r + "     " + u + "Description" + r,
				ANSI_BG_GRAY + "two          8  (8)  Task 7     " + ANSI_RESET,
				"one          4  (4)  Task 3     ",
				"",
				ANSI_BOLD + "Total Tasks: 2" + ANSI_RESET,
				""
		);
	}

	@Test
	void tasks_in_active_project() {
		tasks.getActiveContext().setActiveProject(new ExistingProject(projects, "micro-task"));

		commands.execute(printStream, "tasks");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Tasks in active project 'micro-task'",
				"",
				u + "List" + r + "     " + u + "Type" + r + "  " + u + "ID" + r + "  " + u + r + "     " + u + "Description" + r,
				ANSI_BG_GRAY + "two             9  (9)  Task 8     " + ANSI_RESET,
				"two             8  (8)  Task 7     ",
				ANSI_BG_GRAY + "two             7  (7)  Task 6     " + ANSI_RESET,
				"two             6  (6)  Task 5     ",
				ANSI_BG_GRAY + "one             5  (5)  Task 4     " + ANSI_RESET,
				"one             4  (4)  Task 3     ",
				ANSI_BG_GRAY + "one             3  (3)  Task 2     " + ANSI_RESET,
				"one             2  (2)  Task 1     ",
				ANSI_BG_GRAY + "general   R     1  (1)  Planning   " + ANSI_RESET,
				"",
				ANSI_BOLD + "Total Tasks: 9 (1 Recurring)" + ANSI_RESET,
				""
		);
	}

	@Test
	void tasks_in_active_project_with_tags() {
		tasks.getActiveContext().setActiveProject(new ExistingProject(projects, "micro-task"));
		tasks.getActiveContext().setActiveTags(Arrays.asList("alpha", "bravo"));

		commands.execute(printStream, "tasks");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Tasks in active project 'micro-task' with tag(s): alpha, bravo",
				"",
				u + "List" + r + "  " + u + "Type" + r + "  " + u + "ID" + r + "  " + u + r + "     " + u + "Description" + r,
				ANSI_BG_GRAY + "two          8  (8)  Task 7     " + ANSI_RESET,
				"one          4  (4)  Task 3     ",
				"",
				ANSI_BOLD + "Total Tasks: 2" + ANSI_RESET,
				""
		);
	}

	@Test
	void tasks_in_active_feature() {
		Project project = projects.getProject(new ExistingProject(projects, "micro-task"));
		tasks.getActiveContext().setActiveFeature(ExistingFeature.tryCreate(project, "one"));

		commands.execute(printStream, "tasks");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Tasks for active feature 'one' of project 'micro-task'",
				"",
				u + "List" + r + "  " + u + "Type" + r + "  " + u + "ID" + r + "  " + u + r + "     " + u + "Description" + r,
				ANSI_BG_GRAY + "one          5  (5)  Task 4     " + ANSI_RESET,
				"one          4  (4)  Task 3     ",
				ANSI_BG_GRAY + "one          3  (3)  Task 2     " + ANSI_RESET,
				"one          2  (2)  Task 1     ",
				"",
				ANSI_BOLD + "Total Tasks: 4" + ANSI_RESET,
				""
		);
	}

	@Test
	void tasks_in_active_feature_with_tags() {
		Project project = projects.getProject(new ExistingProject(projects, "micro-task"));
		tasks.getActiveContext().setActiveFeature(ExistingFeature.tryCreate(project, "one"));
		tasks.getActiveContext().setActiveTags(Arrays.asList("alpha", "bravo"));

		commands.execute(printStream, "tasks");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Tasks for active feature 'one' of project 'micro-task' with tag(s): alpha, bravo",
				"",
				u + "List" + r + "  " + u + "Type" + r + "  " + u + "ID" + r + "  " + u + r + "     " + u + "Description" + r,
				ANSI_BG_GRAY + "one          4  (4)  Task 3     " + ANSI_RESET,
				"",
				ANSI_BOLD + "Total Tasks: 1" + ANSI_RESET,
				""
		);
	}

	@Test
	void tasks_in_active_milestone() {
		Project project = projects.getProject(new ExistingProject(projects, "micro-task"));
		tasks.getActiveContext().setActiveMilestone(new ExistingMilestone(project, "20.9.3"));

		commands.execute(printStream, "tasks");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Tasks for active milestone '20.9.3' for project 'micro-task'",
				"",
				u + "List" + r + "  " + u + "Type" + r + "  " + u + "ID" + r + "  " + u + r + "     " + u + "Description" + r,
				ANSI_BG_GRAY + "two          9  (9)  Task 8     " + ANSI_RESET,
				"two          8  (8)  Task 7     ",
				ANSI_BG_GRAY + "two          7  (7)  Task 6     " + ANSI_RESET,
				"two          6  (6)  Task 5     ",
				"",
				ANSI_BOLD + "Total Tasks: 4" + ANSI_RESET,
				""
		);
	}

	@Test
	void tasks_in_active_milestone_with_tags() {
		Project project = projects.getProject(new ExistingProject(projects, "micro-task"));
		tasks.getActiveContext().setActiveMilestone(new ExistingMilestone(project, "20.9.3"));
		tasks.getActiveContext().setActiveTags(Arrays.asList("alpha", "bravo"));

		commands.execute(printStream, "tasks");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Tasks for active milestone '20.9.3' for project 'micro-task' with tag(s): alpha, bravo",
				"",
				u + "List" + r + "  " + u + "Type" + r + "  " + u + "ID" + r + "  " + u + r + "     " + u + "Description" + r,
				ANSI_BG_GRAY + "two          8  (8)  Task 7     " + ANSI_RESET,
				"",
				ANSI_BOLD + "Total Tasks: 1" + ANSI_RESET,
				""
		);
	}
}
