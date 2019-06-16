// Copyright (C) 2019 Andrew Auclair - All Rights Reserved

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class Tasks {
	private int startingID = 0;
	
	private int activeTaskID = -1;
	
	static final class Task {
		final int id;
		final String task;
		
		Task(int id, String task) {
			this.id = id;
			this.task = task;
		}
		
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Task task1 = (Task) o;
			return id == task1.id &&
					Objects.equals(task, task1.task);
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(id, task);
		}
		
		@Override
		public String toString() {
			return id + " - \"" + task + "\"";
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
	
	void finishTask(int taskID) {
		boolean removed = tasks.removeIf(task -> task.id == taskID);
		
		if (!removed) {
			throw new RuntimeException("Task " + taskID + " was not found.");
		}
	}
	
	int getActiveTask() {
		return activeTaskID;
	}
	
	List<Task> getTasks() {
		return tasks;
	}
}
