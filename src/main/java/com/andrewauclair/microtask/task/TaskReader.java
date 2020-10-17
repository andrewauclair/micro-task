// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.build.TaskBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

@SuppressWarnings("CanBeFinal")
public class TaskReader {
	private final OSInterface osInterface;

	public TaskReader(OSInterface osInterface) {
		this.osInterface = osInterface;
	}

	Task readTask(long id, String fileName) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(osInterface.createInputStream(fileName)))) {
			return readTask(id, reader);
		}
//		catch (IOException e) {
//			e.printStackTrace(System.out);
//		}
	}

	Task readTask(long id, BufferedReader reader) throws IOException {
//		try (Scanner scanner = new Scanner(osInterface.createInputStream(fileName))) {
			TaskBuilder builder = new TaskBuilder(id)
					.withName(reader.readLine())
					.withState(TaskState.valueOf(reader.readLine()))
					.withRecurring(Boolean.parseBoolean(reader.readLine()));

			long start = 0;
			long stop = TaskTimes.TIME_NOT_SET;

			String timeProject = "";
			String timeFeature = "";

			boolean foundStartTime = false;

			String line = reader.readLine();

//			while (line != null && !line.startsWith("END")) {
			do {
//				line = reader.readLine();

				if (line.startsWith("start")) {
					start = Integer.parseInt(line.substring(6));
					stop = TaskTimes.TIME_NOT_SET;

					foundStartTime = true;

					timeProject = reader.readLine();
					timeFeature = reader.readLine();
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

				line = reader.readLine();
			} while (line != null && !line.startsWith("END"));

			if (foundStartTime && stop == TaskTimes.TIME_NOT_SET) {
				builder.withTime(new TaskTimes(start, stop, timeProject, timeFeature));
			}

			return builder.build();
//		}
	}
}
