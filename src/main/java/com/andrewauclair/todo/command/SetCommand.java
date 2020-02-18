// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.jline.GroupCompleter;
import com.andrewauclair.todo.jline.ListCompleter;
import com.andrewauclair.todo.task.*;
import picocli.CommandLine;

public abstract class SetCommand extends Command {

	static class SetTaskCommand extends SetCommand {

		@CommandLine.Option(required = true, names = {"--task"})
		private Integer id;

		@CommandLine.Option(names = {"-r", "--recurring"})
		private Boolean recurring;

		@CommandLine.Option(names = {"--not-recurring"})
		private Boolean not_recurring;

		@CommandLine.Option(names = {"--inactive"})
		private boolean inactive;

		private final Tasks tasks;

		SetTaskCommand(Tasks tasks) {
			this.tasks = tasks;
		}

		@Override
		public void run() {
			if (recurring != null) {
				tasks.setRecurring((long) id, true);

				System.out.println("Set recurring for task " + tasks.getTask(id).description() + " to true");
				System.out.println();
			}
			else if (not_recurring != null) {
				tasks.setRecurring((long) id, false);

				System.out.println("Set recurring for task " + tasks.getTask(id).description() + " to false");
				System.out.println();
			}
			else {
				Task task = tasks.setTaskState((long) id, TaskState.Inactive);

				System.out.println("Set state of task " + task.description() + " to Inactive");
				System.out.println();
			}
		}
	}

	static class SetListCommand extends SetCommand {
		@CommandLine.Option(required = true, names = {"-l", "--list"}, completionCandidates = ListCompleter.class)
		private String list;

		@CommandLine.ArgGroup(exclusive = false, multiplicity = "1")
		private ProjectFeature projectFeature;

		private final Tasks tasks;

		SetListCommand(Tasks tasks) {
			this.tasks = tasks;
		}

		@Override
		public void run() {
			if (projectFeature.project != null) {
				TaskList listByName = tasks.getListByName(this.list);
				String project = this.projectFeature.project;
				tasks.setProject(listByName, project, true);

				System.out.println("Set project for list '" + listByName.getFullPath() + "' to '" + project + "'");
			}

			if (projectFeature.feature != null) {
				TaskList listByName = tasks.getListByName(this.list);
				String feature = this.projectFeature.feature;
				tasks.setFeature(listByName, feature, true);

				System.out.println("Set feature for list '" + listByName.getFullPath() + "' to '" + feature + "'");
			}

			System.out.println();
		}
	}

	static class SetGroupCommand extends SetCommand {
		@CommandLine.Option(required = true, names = {"-g", "--group"}, completionCandidates = GroupCompleter.class)
		private String group;

		@CommandLine.ArgGroup(exclusive = false, multiplicity = "1")
		private ProjectFeature projectFeature;

		private final Tasks tasks;

		SetGroupCommand(Tasks tasks) {
			this.tasks = tasks;
		}

		@Override
		public void run() {
			if (projectFeature.project != null) {
				TaskGroup group = tasks.getGroup(this.group);
				String project = this.projectFeature.project;
				tasks.setProject(group, project, true);

				System.out.println("Set project for group '" + group.getFullPath() + "' to '" + project + "'");
			}

			if (projectFeature.feature != null) {
				TaskGroup group = tasks.getGroup(this.group);
				String feature = this.projectFeature.feature;
				tasks.setFeature(group, feature, true);

				System.out.println("Set feature for group '" + group.getFullPath() + "' to '" + feature + "'");
			}

			System.out.println();
		}
	}

	static class ProjectFeature {
		@CommandLine.Option(names = {"-p", "--project"})
		private String project;

		@CommandLine.Option(names = {"-f", "--feature"})
		private String feature;
	}
}
