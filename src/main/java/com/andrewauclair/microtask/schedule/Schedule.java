// Copyright (C) 2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.schedule;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.project.ExistingProject;
import com.andrewauclair.microtask.project.Project;
import com.andrewauclair.microtask.project.Projects;
import com.andrewauclair.microtask.task.ExistingID;
import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskState;
import com.andrewauclair.microtask.task.Tasks;
import org.eclipse.jgit.ignore.internal.Strings;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Schedule {
	private final Map<Project, Integer> percents = new HashMap<>();
	private final Tasks tasks;
	private final OSInterface osInterface;

	private final List<Long> daily_tasks = new ArrayList<>();

	public Schedule(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}

	public void scheduleProject(Project project, int percent) {
		percents.put(project, percent);

		saveSchedule();
	}

	public List<Task> tasks() {
		List<Task> tasks = new ArrayList<>();

		for (long task_id : daily_tasks) {
			tasks.add(this.tasks.getTask(new ExistingID(this.tasks.idValidator(), task_id)));
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
							.sorted(Comparator.comparingLong(o -> o.dueTime))
							.map(task -> task.ID())
							.limit(task_count)
							.collect(Collectors.toList());

					daily_tasks.addAll(tasks_for_day_for_project);

				});

		int remaining_tasks = TASKS_PER_DAY - daily_tasks.size();

		List<Task> allTasks = tasks.getAllTasks().stream()
				.sorted(Comparator.comparingLong(o -> o.dueTime))
				.collect(Collectors.toList());
		allTasks.removeAll(tasks.getGroup("/projects/").getTasks());

		daily_tasks.addAll(allTasks.stream()
				.filter(task -> task.state != TaskState.Finished)
				.filter(task -> !task.recurring)
				.map(task -> task.ID())
				.sorted()
				.limit(remaining_tasks)
				.collect(Collectors.toList()));
	}

	private void saveSchedule() {
		try (PrintStream output = new PrintStream(osInterface.createOutputStream("git-data/schedule.txt"))) {
			List<Project> projects = percents.keySet().stream().sorted((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName()))
					.collect(Collectors.toList());

			for (final Project project : projects) {
				output.print(project.getName());
				output.print(" ");
				output.println(percents.get(project));
			}
		}
		catch (IOException e) {
			e.printStackTrace(System.out);
		}
	}

	public void loadSchedule(Projects projects) {
		if (!osInterface.fileExists("git-data/schedule.txt")) {
			return;
		}

		try (Scanner scanner = new Scanner(osInterface.createInputStream("git-data/schedule.txt"))) {
			percents.clear();

			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();

				List<String> split = Strings.split(line, ' ');

				percents.put(projects.getProject(new ExistingProject(projects, split.get(0))), Integer.parseInt(split.get(1)));
			}
		}
		catch (IOException e) {
			e.printStackTrace(System.out);
		}

	}
}
