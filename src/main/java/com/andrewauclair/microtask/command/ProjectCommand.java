// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.ConsoleTable;
import com.andrewauclair.microtask.LocalSettings;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.project.ExistingProject;
import com.andrewauclair.microtask.project.Project;
import com.andrewauclair.microtask.project.Projects;
import com.andrewauclair.microtask.task.*;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.ArrayList;
import java.util.List;

import static com.andrewauclair.microtask.ConsoleTable.Alignment.LEFT;
import static com.andrewauclair.microtask.ConsoleTable.Alignment.RIGHT;

@Command(name = "project")
public class ProjectCommand implements Runnable {
	private final Tasks tasks;
	private final Projects projects;
	private final LocalSettings localSettings;
	private final OSInterface osInterface;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@Option(names = {"-n", "--name"})
	private ExistingProject name;

	@Option(names = {"--progress"})
	private boolean progress;

	@Option(names = {"--list"})
	private boolean list;

	@Option(names = {"-v", "--verbose"})
	private boolean verbose;

	public ProjectCommand(Tasks tasks, Projects projects, LocalSettings localSettings, OSInterface osInterface) {
		this.tasks = tasks;
		this.projects = projects;
		this.localSettings = localSettings;
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		if (list) {
			for (final Project project : projects.getAllProjects()) {
				System.out.println(project.getName());
			}
		}
		else if (progress) {
			System.out.println("Project progress for 'test'");
			System.out.println();

			Project project = projects.getProject(name);

			ConsoleTable table = new ConsoleTable(osInterface);
			table.setColumnAlignment(LEFT, RIGHT, LEFT, RIGHT, LEFT, RIGHT);
			table.setCellSpacing(1);

//			List<List<String>> output = new ArrayList<>();

			table.addRow(
					"Features",
					String.valueOf(project.getFinishedFeatureCount()),
					"/",
					String.valueOf(project.getFeatureCount()),
					progressBar(project.getFinishedFeatureCount(), project.getFeatureCount()),
					String.format("%d %%", (int) ((project.getFinishedFeatureCount() / (double) project.getFeatureCount()) * 100))
			);
			table.addRow(
					"Tasks",
					String.valueOf(project.getFinishedTaskCount()),
					"/",
					String.valueOf(project.getTaskCount()),
					progressBar(project.getFinishedTaskCount(), project.getTaskCount()),
					String.format("%d %%", (int) ((project.getFinishedTaskCount() / (double) project.getTaskCount()) * 100))
			);
			table.addRow();
//			output.add(Collections.emptyList());

			addTaskOutput(table, project.getGroup());

//			printOutput(output);
			table.print();
		}
		System.out.println();
	}

	private void addTaskOutput(ConsoleTable table, TaskGroup group) {
//		List<List<String>> output = new ArrayList<>();

		for (final TaskContainer child : group.getChildren()) {
			if (child.getState() == TaskContainerState.Finished && !verbose) {
				continue;
			}
			long finished = child.getTasks().stream()
					.filter(task -> task.state == TaskState.Finished)
					.count();

			int percent = (int) ((finished / (double) child.getTasks().size()) * 100);

			table.addRow(
					child.getFullPath(),
					String.valueOf(finished),
					"/",
					String.valueOf(child.getTasks().size()),
					progressBar(finished, child.getTasks().size()),
					String.format("%d %%", percent)
			);

			if (child instanceof TaskGroup childGroup) {
				addTaskOutput(table, childGroup);
			}
		}
	}

	private String progressBar(long finished, long total) {
		int percent = (int) ((finished / (double) total) * 100);

		return "[" +
				"=".repeat(Math.max(0, percent / 10)) +
				" ".repeat(Math.max(0, 10 - (percent / 10))) +
				"]";
	}

	private void printOutput(List<List<String>> output) {
		List<Integer> widths = new ArrayList<>();

		for (final String str : output.get(0)) {
			widths.add(0);
		}

		for (final List<String> strings : output) {
			for (int i = 0; i < strings.size(); i++) {
				if (widths.size() < i + 1) {
					widths.add(0);
				}
				if (strings.get(i).length() > widths.get(i)) {
					widths.set(i, strings.get(i).length());
				}
			}
		}

		for (final List<String> strings : output) {
			for (int i = 0; i < strings.size(); i++) {
				if (i == 0) {
					System.out.print(String.format("%-" + widths.get(i) + "s ", strings.get(i)));
				}
				else {
					System.out.print(String.format("%" + widths.get(i) + "s ", strings.get(i)));
				}
			}
			System.out.println();
		}
	}
}
