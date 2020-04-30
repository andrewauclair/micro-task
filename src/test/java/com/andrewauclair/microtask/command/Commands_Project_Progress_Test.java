package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.project.Project;
import org.junit.jupiter.api.Test;

public class Commands_Project_Progress_Test extends CommandsBaseTestCase {
	@Test
	void show_progress_for_features() {
		tasks.addGroup(newGroup("/test/"));
		tasks.addList(newList("/test/one"), true);
		tasks.setActiveList(existingList("/test/one"));

		for (int i = 0; i < 10; i++) {
			tasks.addTask("Test");
		}

		projects.createProject(existingGroup("/test/"));

		commands.execute(System.out, "project --name test --progress");

		assertOutput(
				"Project progress for 'test'",
				"",
				"Features  0 /  1 [          ] 0% ",
				"Tasks     0 / 10 [          ] 0% ",
				"",
				"/test/one 0 / 10 [          ] 0% ",
				""
		);
	}

	@Test
	void show_count_of_finished_tasks() {
		tasks.addGroup(newGroup("/test/"));
		tasks.addList(newList("/test/one"), true);
		tasks.setActiveList(existingList("/test/one"));

		for (int i = 0; i < 10; i++) {
			tasks.addTask("Test");
		}

		tasks.finishTask(existingID(1));
		tasks.finishTask(existingID(2));

		projects.createProject(existingGroup("/test/"));

		commands.execute(System.out, "project --name test --progress");

		assertOutput(
				"Project progress for 'test'",
				"",
				"Features  0 /  1 [          ]  0% ",
				"Tasks     2 / 10 [==        ] 20% ",
				"",
				"/test/one 2 / 10 [==        ] 20% ",
				""
		);
	}

	@Test
	void displays_all_sub_features() {
		tasks.addGroup(newGroup("/test/"));
		tasks.addGroup(newGroup("/test/four/"));
		tasks.addList(newList("/test/one"), true);
		tasks.addList(newList("/test/two"), true);
		tasks.addList(newList("/test/three"), true);
		tasks.addList(newList("/test/four/five"), true);

		tasks.setActiveList(existingList("/test/one"));
		tasks.addTask("Test");
		tasks.addTask("Test");
		tasks.addTask("Test");
		tasks.setActiveList(existingList("/test/two"));
		tasks.addTask("Test");
		tasks.addTask("Test");
		tasks.setActiveList(existingList("/test/three"));
		tasks.addTask("Test");
		tasks.addTask("Test");
		tasks.addTask("Test");
		tasks.addTask("Test");
		tasks.setActiveList(existingList("/test/four/five"));
		tasks.addTask("Test");

		tasks.finishTask(existingID(10));
		tasks.finishList(existingList("/test/four/five"));

		projects.createProject(existingGroup("/test/"));

		commands.execute(System.out, "project --name test --progress");

		assertOutput(
				"Project progress for 'test'",
				"",
				"Features        1 /  5 [==        ]  20% ",
				"Tasks           1 / 10 [=         ]  10% ",
				"",
				"/test/four/     1 /  1 [==========] 100% ",
				"/test/four/five 1 /  1 [==========] 100% ",
				"/test/one       0 /  3 [          ]   0% ",
				"/test/two       0 /  2 [          ]   0% ",
				"/test/three     0 /  4 [          ]   0% ",
				""
		);
	}
}
