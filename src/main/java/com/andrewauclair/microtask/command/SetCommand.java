// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.jline.GroupCompleter;
import com.andrewauclair.microtask.jline.ListCompleter;
import com.andrewauclair.microtask.task.*;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

abstract class SetCommand implements Runnable {
	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	static final class SetTaskCommand extends SetCommand {
		private final Tasks tasks;

		@Option(required = true, names = {"--task"})
		private Integer id;

		@Option(names = {"-r", "--recurring"})
		private Boolean recurring;

		@Option(names = {"--not-recurring"})
		private Boolean not_recurring;

		@Option(names = {"--inactive"})
		private boolean inactive;

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

	static final class SetListCommand extends SetCommand {
		private final Tasks tasks;

		@Option(required = true, names = {"-l", "--list"}, completionCandidates = ListCompleter.class)
		private String list;

		@ArgGroup(exclusive = false, multiplicity = "1")
		private ProjectFeature projectFeature;

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

	static final class SetGroupCommand extends SetCommand {
		private final Tasks tasks;

		@Option(required = true, names = {"-g", "--group"}, completionCandidates = GroupCompleter.class)
		private String group;

		@ArgGroup(exclusive = false, multiplicity = "1")
		private ProjectFeature projectFeature;

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

	static final class ProjectFeature {
		@Option(names = {"-p", "--project"})
		private String project;

		@Option(names = {"-f", "--feature"})
		private String feature;
	}
}
