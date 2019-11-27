// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import com.andrewauclair.todo.os.OSInterface;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TaskReader {
	private final OSInterface osInterface;
	
	public TaskReader(OSInterface osInterface) {
		this.osInterface = osInterface;
	}

	Task readTask(long id, String fileName) throws IOException {
		InputStream inputStream = osInterface.createInputStream(fileName);

		Scanner scanner = new Scanner(inputStream);

		String task;
		TaskState state;

		task = scanner.nextLine();
		state = TaskState.valueOf(scanner.nextLine());

		boolean recurring = Boolean.parseBoolean(scanner.nextLine());
		
		long start = 0;
		long stop = TaskTimes.TIME_NOT_SET;
		String timeProject = "";
		String timeFeature = "";
		
		List<TaskTimes> timesList = new ArrayList<>();

		boolean readTimes = false;
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.startsWith("start")) {
				start = Integer.parseInt(line.substring(6));
				stop = TaskTimes.TIME_NOT_SET;

				readTimes = true;
			}
			else if (line.startsWith("stop")) {
				stop = Integer.parseInt(line.substring(5));
				
				timesList.add(new TaskTimes(start, stop, timeProject, timeFeature));
				
				timeProject = "";
				timeFeature = "";
			}
			else if (line.startsWith("add")) {
				long add = Integer.parseInt(line.substring(4));

				timesList.add(new TaskTimes(add));
			}
			else if (!line.isEmpty()) {
				timeProject = line;
				timeFeature = scanner.nextLine();
			}
		}

		if (readTimes && stop == TaskTimes.TIME_NOT_SET) {
			timesList.add(new TaskTimes(start, stop, timeProject, timeFeature));
		}

		scanner.close();
		
		return new Task(id, task, state, timesList, recurring);
	}
}
