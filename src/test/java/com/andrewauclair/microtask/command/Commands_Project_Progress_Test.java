package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.project.Project;
import org.junit.jupiter.api.Test;

public class Commands_Project_Progress_Test extends CommandsBaseTestCase {
	@Test
	void show_progress_for_features() {
		for (int i = 0; i < 10; i++) {
			tasks.addTask("Test");
		}

		Project test = projects.createProject("test");

		test.addFeature("one", null);
		test.addFeature("two", null);

		test.getFeature("one").get()
				.addList(existingList("/default"));

		commands.execute(System.out, "project --name test --progress");

		assertOutput(
				"Project progress for 'test'",
				"",
				"Features  0 / 2 [          ] 0%",
				"Tasks     0 / 10 [          ] 0%",
				""
		);
	}

	@Test
	void show_count_of_finished_tasks() {
		for (int i = 0; i < 10; i++) {
			tasks.addTask("Test");
		}

		tasks.finishTask(existingID(1));
		tasks.finishTask(existingID(2));

		Project test = projects.createProject("test");

		test.addFeature("one", null);
		test.addFeature("two", null);

		test.getFeature("one").get()
				.addList(existingList("/default"));

		commands.execute(System.out, "project --name test --progress");

		assertOutput(
				"Project progress for 'test'",
				"",
				"Features  0 / 2 [          ] 0%",
				"Tasks     2 / 10 [==        ] 20%",
				""
		);
	}
}
