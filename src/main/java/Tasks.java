// Copyright (C) 2019 Andrew Auclair - All Rights Reserved

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class Tasks {
	private int startingID = 0;
	
	private int activeTaskID = -1;
	
	class Task {
		int id;
		String task;
		
		Task(int id, String task) {
			this.id = id;
			this.task = task;
		}
	}
	
	private List<Task> tasks = new ArrayList<>();
	
	int addTask(String task) {
		Task newTask = new Task(startingID++, task);
		tasks.add(newTask);
		return newTask.id;
	}
	
	void startTask(int id) {
		tasks.stream()
				.filter(p -> p.id == id)
				.findFirst()
				.ifPresent(p -> activeTaskID = p.id);
	}
	
	int getActiveTask() {
		return activeTaskID;
	}
	
	List<String> getTasks() {
		return tasks.stream()
				.map(p -> p.task)
				.collect(Collectors.toList());
	}
}
