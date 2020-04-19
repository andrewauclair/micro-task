// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.build.TaskBuilder;

import java.io.IOException;
import java.util.Scanner;

@SuppressWarnings("CanBeFinal")
public class TaskReader {
	private final OSInterface osInterface;

	public TaskReader(OSInterface osInterface) {
		this.osInterface = osInterface;
	}

	Task readTask(long id, String fileName) throws IOException {
		try (Scanner scanner = new Scanner(osInterface.createInputStream(fileName))) {
			TaskBuilder builder = new TaskBuilder(id)
					.withName(scanner.nextLine())
					.withState(TaskState.valueOf(scanner.nextLine()))
					.withRecurring(Boolean.parseBoolean(scanner.nextLine()));

			long start = 0;
			long stop = TaskTimes.TIME_NOT_SET;

			String timeProject = "";
			String timeFeature = "";

			boolean foundStartTime = false;

			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();

				if (line.startsWith("start")) {
					start = Integer.parseInt(line.substring(6));
					stop = TaskTimes.TIME_NOT_SET;

					foundStartTime = true;

					timeProject = scanner.nextLine();
					timeFeature = scanner.nextLine();
				}
				else if (line.startsWith("stop")) {
					stop = Integer.parseInt(line.substring(5));

					builder.withTime(new TaskTimes(start, stop, timeProject, timeFeature));
				}
				else if (line.startsWith("add")) {
					long add = Integer.parseInt(line.substring(4));

					builder.withTime(new TaskTimes(add));
				}
				else if (line.startsWith("finish")) {
					long finish = Integer.parseInt(line.substring(7));

					builder.withTime(new TaskTimes(finish));
				}
			}

			if (foundStartTime && stop == TaskTimes.TIME_NOT_SET) {
				builder.withTime(new TaskTimes(start, stop, timeProject, timeFeature));
			}

			return builder.build();
		}
	}
}
