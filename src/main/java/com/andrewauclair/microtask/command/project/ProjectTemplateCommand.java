// Copyright (C) 2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.project;

import com.andrewauclair.microtask.os.OSInterface;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@CommandLine.Command(name = "template")
public class ProjectTemplateCommand implements Runnable {
	private final OSInterface osInterface;

	@CommandLine.Option(names = {"--create"})
	String template_name;

	private static class TemplateGroup {
		String name;
		List<TemplateList> lists = new ArrayList<>();

		private void print() {
			System.out.println(name);

			for (final TemplateList list : lists) {
				list.print();
			}
		}
	}
	private static class TemplateList {
		String name;
		List<TemplateTask> tasks = new ArrayList<>();

		private void print() {
			System.out.println(name);

			for (final TemplateTask task : tasks) {
				task.print();
			}
		}
	}
	private static class TemplateTask {
		String name;
		boolean recurring;

		private void print() {
			System.out.print("'" + name + "'");
			if (recurring) {
				System.out.print(" recurring");
			}
			System.out.println();
		}
	}
	private static class Template {
		List<TemplateGroup> groups = new ArrayList<>();

		private void print() {
			for (final TemplateGroup group : groups) {
				group.print();
			}
		}
	}

	public ProjectTemplateCommand(OSInterface osInterface) {
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		Template template = new Template();
		template.groups = create_groups();

		System.out.println("Created project template '" + template_name + "'");
		System.out.println();
		template.print();
	}

	private List<TemplateGroup> create_groups() {
		List<TemplateGroup> groups = new ArrayList<>();

		boolean add_group = osInterface.promptChoice("add group");

		while (add_group) {
			String name = osInterface.promptForString("group name", s -> true);

			TemplateGroup group = new TemplateGroup();
			group.name = name;
			group.lists = create_lists();

			groups.add(group);

			add_group = osInterface.promptChoice("add group");
		}
		return groups;
	}

	private List<TemplateList> create_lists() {
		List<TemplateList> lists = new ArrayList<>();

		boolean add_list = osInterface.promptChoice("add list");

		while (add_list) {
			String name = osInterface.promptForString("list name", s -> true);

			TemplateList list = new TemplateList();
			list.name = name;
			list.tasks = create_tasks();
			lists.add(list);

			add_list = osInterface.promptChoice("add list");
		}
		return lists;
	}

	private List<TemplateTask> create_tasks() {
		List<TemplateTask> tasks = new ArrayList<>();

		boolean add_task = osInterface.promptChoice("add task");

		while (add_task) {
			String name = osInterface.promptForString("task name: ", s -> !s.contains("\""));

			boolean recurring = osInterface.promptChoice("recurring");

			TemplateTask task = new TemplateTask();
			task.name = name;
			task.recurring = recurring;

			tasks.add(task);

			add_task = osInterface.promptChoice("add task");
		}
		return tasks;
	}
}
