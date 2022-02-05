// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.project;

import com.andrewauclair.microtask.task.Task;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Feature_Tasks_Test extends ProjectBaseTestCase {
	@Test
	void feature_contains_tasks_for_list() {
		Feature feature = new Feature(osInterface, project, tasks, "one", null);

		tasks.addGroup(newGroup("/one/"));
		tasks.addList(newList("/one/two"), true);
		tasks.setCurrentList(existingList("/one/two"));

		Task task1 = tasks.addTask("Test");
		Task task2 = tasks.addTask("Test");
		Task task3 = tasks.addTask("Test");

		feature.addList(existingList("/one/two"));

		assertThat(feature.getTasks()).containsOnly(
				task1,
				task2,
				task3
		);
	}

	@Test
	void feature_contains_tasks_from_group() {
		Feature feature = new Feature(osInterface, project, tasks, "one", null);

		tasks.addGroup(newGroup("/one/"));
		tasks.addList(newList("/one/two"), true);
		tasks.setCurrentList(existingList("/one/two"));

		Task task1 = tasks.addTask("Test");
		Task task2 = tasks.addTask("Test");

		tasks.addGroup(newGroup("/one/test/"));
		tasks.addList(newList("/one/test/three"), true);
		tasks.setCurrentList(existingList("/one/test/three"));

		Task task3 = tasks.addTask("Test");

		feature.addGroup(existingGroup("/one/"));

		assertThat(feature.getTasks()).containsOnly(
				task1,
				task2,
				task3
		);
	}

	@Test
	void feature_does_not_count_tasks_twice() {
		Feature feature = new Feature(osInterface, project, tasks, "one", null);

		tasks.addGroup(newGroup("/one/"));
		tasks.addList(newList("/one/two"), true);
		tasks.setCurrentList(existingList("/one/two"));

		Task task1 = tasks.addTask("Test");
		Task task2 = tasks.addTask("Test");

		tasks.addGroup(newGroup("/one/test/"));
		tasks.addList(newList("/one/test/three"), true);
		tasks.setCurrentList(existingList("/one/test/three"));

		Task task3 = tasks.addTask("Test");

		feature.addGroup(existingGroup("/one/"));
		feature.addGroup(existingGroup("/one/test/"));
		feature.addList(existingList("/one/test/three"));

		assertThat(feature.getTasks()).hasSize(3);
		assertThat(feature.getTasks()).containsOnly(
				task1,
				task2,
				task3
		);
	}
}
