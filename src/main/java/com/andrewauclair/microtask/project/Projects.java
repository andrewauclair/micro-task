// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.project;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.group.name.ExistingGroupName;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Projects {
	private final List<Project> projects = new ArrayList<>();
	private final Tasks tasks;
	private final OSInterface osInterface;

	private Project activeProject = null;

	public Projects(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}

	public List<Project> getAllProjects() {
		return Collections.unmodifiableList(projects);
	}

	public boolean hasProject(String name) {
		return projects.stream()
				.anyMatch(project -> project.getName().equals(name));
	}

	public Project createProject(ExistingGroupName group) {
		Project project = new Project(tasks, group);
		projects.add(project);

		try (PrintStream outputStream = new PrintStream(osInterface.createOutputStream("git-data/projects.txt"))) {
			for (Project proj : projects) {
				outputStream.println(proj.getGroup().getFullPath());
			}
		}
		catch (IOException e) {
			e.printStackTrace(System.out);
		}

		return project;
	}

	public Project getProject(String name) {
		return projects.stream()
				.filter(project -> project.getName().equals(name))
				.findFirst().get();
	}

	public void setActiveProject(String name) {
		activeProject = getProject(name);
	}

	public Project getActiveProject() {
		return activeProject;
	}

	public void load() {
		try (Scanner scanner = new Scanner(osInterface.createInputStream("git-data/projects.txt"))) {
			while (scanner.hasNextLine()) {
				createProject(new ExistingGroupName(tasks, scanner.nextLine()));
			}
		}
		catch (IOException e) {
			e.printStackTrace(System.out);
		}
	}
}
