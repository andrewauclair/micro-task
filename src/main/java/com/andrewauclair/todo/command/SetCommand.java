// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.jline.GroupCompleter;
import com.andrewauclair.todo.jline.ListCompleter;
import com.andrewauclair.todo.task.*;
import picocli.CommandLine;

public abstract class SetCommand extends Command {

	static class SetTaskCommand extends SetCommand {
		@CommandLine.Option(names = {"--task"})
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
			}
			else if (not_recurring != null) {
				tasks.setRecurring((long) id, false);
			}
			else {
				Task task = tasks.setTaskState((long) id, TaskState.Inactive);

				System.out.println("Set state of task " + task.description() + " to Inactive");
				System.out.println();
			}
		}
	}

	static class SetListCommand extends SetCommand {
		@CommandLine.Option(names = {"-l", "--list"}, completionCandidates = ListCompleter.class)
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
				String project = this.projectFeature.project;
				tasks.setProject(tasks.getListByName(this.list), project, true);
			}

			if (projectFeature.feature != null) {
				String feature = this.projectFeature.feature;
				tasks.setFeature(tasks.getListByName(this.list), feature, true);
			}
		}
	}

	static class SetGroupCommand extends SetCommand {
		@CommandLine.Option(names = {"-g", "--group"}, completionCandidates = GroupCompleter.class)
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
				String project = this.projectFeature.project;
				tasks.setProject(tasks.getGroup(this.group), project, true);
			}

			if (projectFeature.feature != null) {
				String feature = this.projectFeature.feature;
				tasks.setFeature(tasks.getGroup(this.group), feature, true);
			}
		}
	}

	static class ProjectFeature {
		@CommandLine.Option(names = {"-p", "--project"})
		private String project;

		@CommandLine.Option(names = {"-f", "--feature"})
		private String feature;
	}
}
