// Copyright (C) 2019 Andrew Auclair - All Rights Reserved

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
				.filter(task -> task.id == id)
				.findFirst()
				.ifPresentOrElse(task -> activeTaskID = task.id, () -> {
					throw new RuntimeException("Task " + id + " was not found.");
				});
	}
	
	Tasks.Task finishTask() {
		Optional<Task> first = tasks.stream()
				.filter(task -> task.id == activeTaskID)
				.findFirst();
		
		tasks.removeIf(task -> task.id == activeTaskID);
		
		if (first.isPresent()) {
			return first.get();
		}
		throw new RuntimeException("No active task.");
	}
	
	Task getActiveTask() {
		Optional<Task> first = tasks.stream()
				.filter(task -> task.id == activeTaskID)
				.findFirst();
		
		if (first.isPresent()) {
			return first.get();
		}
		throw new RuntimeException("No active task.");
	}
	
	List<Task> getTasks() {
		return tasks;
	}
}
