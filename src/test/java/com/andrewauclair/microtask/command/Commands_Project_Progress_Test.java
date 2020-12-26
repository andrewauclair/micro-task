// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.project.NewProject;
import org.junit.jupiter.api.Test;

public class Commands_Project_Progress_Test extends CommandsBaseTestCase {
	@Test
	void show_progress_for_features() {
		tasks.addGroup(newGroup("/projects/test/"));
		tasks.addList(newList("/projects/test/one"), true);
		tasks.setCurrentList(existingList("/projects/test/one"));

		for (int i = 0; i < 10; i++) {
			tasks.addTask("Test");
		}

		projects.createProject(new NewProject(projects, "test"), true);

		commands.execute(System.out, "project --name test --progress");

		assertOutput(
				"Project progress for 'test'",
				"",
				"Features           0 /  1 [          ] 0 %",
				"Tasks              0 / 10 [          ] 0 %",
				"",
				"/projects/test/one 0 / 10 [          ] 0 %",
				""
		);
	}

	@Test
	void show_count_of_finished_tasks() {
		tasks.addGroup(newGroup("/projects/test/"));
		tasks.addList(newList("/projects/test/one"), true);
		tasks.setCurrentList(existingList("/projects/test/one"));

		for (int i = 0; i < 10; i++) {
			tasks.addTask("Test");
		}

		tasks.finishTask(existingID(1));
		tasks.finishTask(existingID(2));

		projects.createProject(new NewProject(projects, "test"), true);

		commands.execute(System.out, "project --name test --progress");

		assertOutput(
				"Project progress for 'test'",
				"",
				"Features           0 /  1 [          ]  0 %",
				"Tasks              2 / 10 [==        ] 20 %",
				"",
				"/projects/test/one 2 / 10 [==        ] 20 %",
				""
		);
	}

	@Test
	void displays_all_sub_features() {
		tasks.addGroup(newGroup("/projects/test/four/"));
		tasks.addList(newList("/projects/test/one"), true);
		tasks.addList(newList("/projects/test/two"), true);
		tasks.addList(newList("/projects/test/three"), true);
		tasks.addList(newList("/projects/test/four/five"), true);

		tasks.setCurrentList(existingList("/projects/test/one"));
		tasks.addTask("Test");
		tasks.addTask("Test");
		tasks.addTask("Test");
		tasks.setCurrentList(existingList("/projects/test/two"));
		tasks.addTask("Test");
		tasks.addTask("Test");
		tasks.setCurrentList(existingList("/projects/test/three"));
		tasks.addTask("Test");
		tasks.addTask("Test");
		tasks.addTask("Test");
		tasks.addTask("Test");
		tasks.setCurrentList(existingList("/projects/test/four/five"));
		tasks.addTask("Test");

		tasks.finishTask(existingID(10));
		tasks.finishList(existingList("/projects/test/four/five"));

		projects.createProject(new NewProject(projects, "test"), true);

		commands.execute(System.out, "project --name test --progress");

		assertOutput(
				"Project progress for 'test'",
				"",
				"Features             1 /  5 [==        ]  20 %",
				"Tasks                1 / 10 [=         ]  10 %",
				"",
				"/projects/test/four/ 1 /  1 [==========] 100 %",
				"/projects/test/one   0 /  3 [          ]   0 %",
				"/projects/test/two   0 /  2 [          ]   0 %",
				"/projects/test/three 0 /  4 [          ]   0 %",
				""
		);
	}

	@Test
	void displays_all_sub_features_verbose() {
		tasks.addGroup(newGroup("/projects/test/four/"));
		tasks.addList(newList("/projects/test/one"), true);
		tasks.addList(newList("/projects/test/two"), true);
		tasks.addList(newList("/projects/test/three"), true);
		tasks.addList(newList("/projects/test/four/five"), true);

		tasks.setCurrentList(existingList("/projects/test/one"));
		tasks.addTask("Test");
		tasks.addTask("Test");
		tasks.addTask("Test");
		tasks.setCurrentList(existingList("/projects/test/two"));
		tasks.addTask("Test");
		tasks.addTask("Test");
		tasks.setCurrentList(existingList("/projects/test/three"));
		tasks.addTask("Test");
		tasks.addTask("Test");
		tasks.addTask("Test");
		tasks.addTask("Test");
		tasks.setCurrentList(existingList("/projects/test/four/five"));
		tasks.addTask("Test");

		tasks.finishTask(existingID(10));
		tasks.finishList(existingList("/projects/test/four/five"));

		projects.createProject(new NewProject(projects, "test"), true);

		commands.execute(System.out, "project --name test --progress -v");

		assertOutput(
				"Project progress for 'test'",
				"",
				"Features                 1 /  5 [==        ]  20 %",
				"Tasks                    1 / 10 [=         ]  10 %",
				"",
				"/projects/test/four/     1 /  1 [==========] 100 %",
				"/projects/test/four/five 1 /  1 [==========] 100 %",
				"/projects/test/one       0 /  3 [          ]   0 %",
				"/projects/test/two       0 /  2 [          ]   0 %",
				"/projects/test/three     0 /  4 [          ]   0 %",
				""
		);
	}
}
