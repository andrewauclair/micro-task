package com.andrewauclair.todo.command;

import com.andrewauclair.todo.task.Task;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

class Commands_Times_BaseTestCase extends CommandsBaseTestCase {
	private static final long SECONDS_IN_DAY = 86400;
	private static final long MINUTE = 60;
	private static final long HOUR = 60 * MINUTE;

	final long june17_8_am = 1560772800;
	final long june18_8_am = june17_8_am + SECONDS_IN_DAY;
	final long june19_8_am = june18_8_am + SECONDS_IN_DAY;
	final long june20_8_am = june19_8_am + SECONDS_IN_DAY;
	final long june21_8_am = june20_8_am + SECONDS_IN_DAY;

	@BeforeEach
	void setup() throws IOException {
		super.setup();

		osInterface.setIncrementTime(false);

		tasks.addList("/one/two/stuff");
		tasks.addList("/one/design");
		tasks.addList("/one/implementation");


		long currentTime = june17_8_am;

		// build up a set of tasks that we can use to verify all the times command outputs

		// add a bunch of tasks for June 20, 2019, then move onto June 21

		addTaskWithTimes("Test 1", currentTime, (10 * MINUTE) + 21);
		addTaskWithTimes("Test 2", currentTime + (3 * HOUR), HOUR + MINUTE + 39);

		tasks.setActiveList("/one/design");

		addTaskWithTimes("Test 3", currentTime + (4 * HOUR), HOUR + (49 * MINUTE) + 15);
		addTaskWithTimes("Test 4", currentTime + (56 * HOUR), (32 * MINUTE) + 2);

		Task recurringTask = addTaskWithTimes("Test 5", currentTime + (4 * HOUR), (32 * MINUTE) + 20);
		tasks.setRecurring(recurringTask.id, true);

		currentTime = june18_8_am;

		addTaskTimes(1, currentTime, 25 * MINUTE + 45);

		addTaskWithTimes("Test 7", currentTime + (30 * MINUTE), (15 * MINUTE) + 24);

		tasks.setActiveList("/one/two/stuff");

		addTaskWithTimes("Test 6", currentTime + HOUR, 20 * MINUTE);

		currentTime = june19_8_am;

		addTaskTimes(1, currentTime, 20 * MINUTE);

		currentTime = june20_8_am;

		addTaskTimes(1, currentTime, 3 * HOUR);

		currentTime = june21_8_am;

		addTaskWithTimes("Test 5", currentTime, HOUR);

		// add task to June 25, 2019 so we can test --week
		currentTime = june21_8_am + (4 * SECONDS_IN_DAY);

		addTaskWithTimes("Next Week", currentTime, HOUR);
		addTaskTimes(1, currentTime, MINUTE);

		tasks.startTask(1, false);
		tasks.finishTask(3);

		tasks.setProject(1, "Project 1");
		tasks.setProject(2, "Project 2");
	}

	Task addTaskWithTimes(String name, long start, long length) {
		Task task = tasks.addTask(name);
		setTime(start);
		tasks.startTask(task.id, false);
		setTime(start + length);
		return tasks.stopTask();
	}

	void addTaskTimes(long id, long start, long length) {
		setTime(start);
		tasks.startTask(id, false);
		setTime(start + length);
		tasks.stopTask();
	}
}
