// Copyright (C) 2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskTimes;
import com.andrewauclair.microtask.task.build.TaskBuilder;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import org.junit.jupiter.api.Test;

class Commands_Time_Tracking_Test extends CommandsBaseTestCase {
	@Test
	void test() {
//		tasks.setGroupTimeCategory();
		tasks.addList(newList("/one"), "overhead-general", true);

		tasks.addGroup(newGroup("/jreap/"));
		tasks.addGroup(newGroup("/link22/"));

		tasks.addList(newList("/zolak"), "zolak-issues", true);
		tasks.addList(newList("/jreap/design"), "jreap-cyberboss-design", true);
		tasks.addList(newList("/jreap/implementation"), "jreap-cyberboss-implementation", true);
		tasks.addList(newList("/link22/design"), "link22-design", true);
		tasks.addList(newList("/link22/implementation"), "link22-implementation", true);

		osInterface.setIncrementTime(false);

		int monday_7am = 1644840000;
		int tuesday_7am = 1644926400;
		int wednesday_7am = 1645012800;
		int thursday_7am = 1645099200;
		int friday_7am = 1645185600;

		addTask("Test 1", existingList("/one"),
				new TaskTimes(tuesday_7am, tuesday_7am + 450));
		addTask("Test 2", existingList("/one"),
				new TaskTimes(wednesday_7am, wednesday_7am + 225));

		addTask("Test 3", existingList("/jreap/design"),
				new TaskTimes(monday_7am, monday_7am + (900 * 3)));
		addTask("Test 4", existingList("/jreap/design"),
				new TaskTimes(thursday_7am, thursday_7am + (900 * 2) + 450));

		addTask("Test 5", existingList("/link22/design"),
				new TaskTimes(monday_7am, monday_7am + 900 + 225),
				new TaskTimes(tuesday_7am, tuesday_7am + (900 * 4)),
				new TaskTimes(friday_7am, friday_7am + 900)
		);
		addTask("Test 6", existingList("/link22/design"),
				new TaskTimes(tuesday_7am, tuesday_7am + (900 * 4)),
				new TaskTimes(friday_7am, friday_7am + 900)
		);

		addTask("Test 7", existingList("/link22/implementation"),
				new TaskTimes(monday_7am, monday_7am + (900 * 2) + 675),
				new TaskTimes(wednesday_7am, wednesday_7am + (900 * 2)),
				new TaskTimes(thursday_7am, thursday_7am + (900 * 3)),
				new TaskTimes(friday_7am, friday_7am + (900 * 2))
		);
		addTask("Test 8", existingList("/link22/implementation"),
				new TaskTimes(wednesday_7am, wednesday_7am + (900 * 2)),
				new TaskTimes(thursday_7am, thursday_7am + (900 * 2) + 400),
				new TaskTimes(friday_7am, friday_7am + (900 * 4))
		);

		addTask("Test 9", existingList("/zolak"),
				new TaskTimes(monday_7am, monday_7am + 900),
				new TaskTimes(wednesday_7am, wednesday_7am + (900 * 3) + 200),
				new TaskTimes(friday_7am, friday_7am + 900)
		);

		osInterface.setTime(friday_7am);

		commands.execute(printStream, "times tracking");

		assertOutput(
				"Time Tracking for Week of 2/13/2022",
				"",
				"                            Su      Mo      Tu      We      Th      Fr      Sa      Total",
				"link22-implementation            2.75h           4.00h   5.50h   6.00h             18.25h",
				"link22-design                    1.25h   8.00h                   2.00h             11.25h",
				"jreap-cyberboss-design           3.00h                   2.50h                      5.50h",
				"zolak-issues                     1.00h           3.25h           1.00h              5.25h",
				"overhead-general                         0.50h   0.25h                              0.75h",
				"",
				"Total                            8.00h   8.50h   7.50h   8.00h   9.00h             41.00h",
				""
		);
	}

	private void addTask(String name, ExistingListName list, TaskTimes... times) {
		TaskBuilder builder = new TaskBuilder(tasks.incrementID())
				.withTask(name);

		for (final TaskTimes time : times) {
			builder.withStartStopTime(time);
		}

		tasks.addTask(builder.build(), tasks.getList(list), true);
	}
}
