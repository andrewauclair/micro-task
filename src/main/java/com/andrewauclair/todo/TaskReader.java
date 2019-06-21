// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.os.OSInterface;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class TaskReader {
	private final OSInterface osInterface;
	
	TaskReader(OSInterface osInterface) {
		this.osInterface = osInterface;
	}
	
	Task readTask(String fileName) throws IOException {
		InputStream inputStream = osInterface.createInputStream(fileName);
		
		Scanner scanner = new Scanner(inputStream);
		
		int id = 0;
		String task = "";
		Task.TaskState state = Task.TaskState.Inactive;
		TaskTimes times = new TaskTimes();
		
		id = Integer.parseInt(fileName.substring(fileName.indexOf('/') + 1, fileName.indexOf(".txt")));
		task = scanner.nextLine();
		state = Task.TaskState.valueOf(scanner.nextLine());
		
		long start = 0;
		long stop = TaskTimes.Times.TIME_NOT_SET;
		List<TaskTimes.Times> timesList = new ArrayList<>();
		
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.startsWith("start")) {
				start = Integer.parseInt(line.substring(6));
				stop = TaskTimes.Times.TIME_NOT_SET;
			}
			if (line.startsWith("stop")) {
				stop = Integer.parseInt(line.substring(5));
				
				timesList.add(new TaskTimes.Times(start, stop));
			}
		}
		
		if (stop == TaskTimes.Times.TIME_NOT_SET) {
			timesList.add(new TaskTimes.Times(start, stop));
		}
		return new Task(id, task, state, new TaskTimes(timesList));
	}
}
