// Copyright (C) 2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.schedule;

import com.andrewauclair.microtask.project.Project;
import com.andrewauclair.microtask.task.ExistingID;
import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskState;
import com.andrewauclair.microtask.task.Tasks;

import java.util.*;
import java.util.stream.Collectors;

public class Schedule {
	private final Map<Project, Integer> percents = new HashMap<>();
	private final Tasks tasks;

	private List<Long> daily_tasks = new ArrayList<>();

	public Schedule(Tasks tasks) {
		this.tasks = tasks;
	}

	public void scheduleProject(Project project, int percent) {
		percents.put(project, percent);
	}

	public List<Task> tasks() {
		List<Task> tasks = new ArrayList<>();

		for (long task_id : daily_tasks) {
			tasks.add(this.tasks.getTask(new ExistingID(this.tasks, task_id)));
		}
		return tasks;
	}

	public void display() {
		System.out.println("Schedule");
		System.out.println();

		percents.entrySet().stream()
				.sorted(Comparator.comparingInt(Map.Entry::getValue))
				.forEach(val -> System.out.printf("%3d %%  %s%n", val.getValue(), val.getKey().getName()));
	}

	public boolean hasProject(String project) {
		return percents.keySet().stream()
				.anyMatch(prj -> project.equals(prj.getName()));
	}

	public int projectPercent(String project) {
		Optional<Map.Entry<Project, Integer>> first = percents.entrySet().stream()
				.filter(set -> project.equals(set.getKey().getName()))
				.findFirst();

		if (first.isPresent()) {
			return first.get().getValue();
		}
		return 0;
	}

	public void createTasksForDay() {
		daily_tasks.clear();

		final int TASKS_PER_DAY = 16;

		percents.entrySet().stream()
				.sorted(Comparator.comparingInt(Map.Entry::getValue))
				.forEach(val -> {
					Project project = val.getKey();

					int task_count = (int) Math.round((val.getValue() / 100.0) * TASKS_PER_DAY);

					List<Long> tasks_for_day_for_project = project.getGroup().getTasks().stream()
							.filter(task -> task.state != TaskState.Finished)
							.filter(task -> !task.recurring)
							.map(task -> task.id)
							.limit(task_count)
							.collect(Collectors.toList());

					daily_tasks.addAll(tasks_for_day_for_project);

				});

		int remaining_tasks = TASKS_PER_DAY - daily_tasks.size();

		List<Task> allTasks = tasks.getAllTasks();
		allTasks.removeAll(tasks.getGroup("/projects/").getTasks());

		daily_tasks.addAll(allTasks.stream()
				.filter(task -> task.state != TaskState.Finished)
				.filter(task -> !task.recurring)
				.map(task -> task.id)
				.limit(remaining_tasks)
				.collect(Collectors.toList()));
	}
}
