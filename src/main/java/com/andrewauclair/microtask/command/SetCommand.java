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
		private Long id;

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
				tasks.setRecurring(id, true);

				System.out.println("Set recurring for task " + tasks.getTask(id).description() + " to true");
			}
			else if (not_recurring != null) {
				tasks.setRecurring(id, false);

				System.out.println("Set recurring for task " + tasks.getTask(id).description() + " to false");
			}
			else {
				Task task = tasks.getTask(id);

				if (task.state == TaskState.Finished) {
					task = tasks.setTaskState(id, TaskState.Inactive);

					System.out.println("Set state of task " + task.description() + " to Inactive");
				}
				else {
					System.out.println("Task " + task.description() + " must be finished first");
				}
			}
			System.out.println();
		}
	}

	static final class SetListCommand extends SetCommand {
		private final Tasks tasks;

		@Option(required = true, names = {"-l", "--list"}, completionCandidates = ListCompleter.class)
		private String list;

		@ArgGroup(exclusive = false, multiplicity = "1")
		SetListArgs args;

		private static class SetListArgs {
			@ArgGroup(exclusive = false)
			private ProjectFeature projectFeature;

			@Option(names = {"--in-progress"})
			private boolean in_progress;
		}

		SetListCommand(Tasks tasks) {
			this.tasks = tasks;
		}

		@Override
		public void run() {
			if (args.projectFeature != null) {
				handleProjectAndFeature();
			}

			if (args.in_progress) {
				TaskList list = tasks.getListByName(this.list);

				if (list.getState() == TaskContainerState.Finished) {
					tasks.setListState(list, TaskContainerState.InProgress, true);

					System.out.println("Set state of list '" + list.getFullPath() + "' to In Progress");
				}
				else {
					System.out.println("List '" + list.getFullPath() + "' must be finished first");
				}
			}

			System.out.println();
		}

		private void handleProjectAndFeature() {
			if (args.projectFeature.project != null) {
				TaskList listByName = tasks.getListByName(this.list);
				String project = this.args.projectFeature.project;
				tasks.setProject(listByName, project, true);

				System.out.println("Set project for list '" + listByName.getFullPath() + "' to '" + project + "'");
			}

			if (args.projectFeature.feature != null) {
				TaskList listByName = tasks.getListByName(this.list);
				String feature = this.args.projectFeature.feature;
				tasks.setFeature(listByName, feature, true);

				System.out.println("Set feature for list '" + listByName.getFullPath() + "' to '" + feature + "'");
			}
		}
	}

	static final class SetGroupCommand extends SetCommand {
		private final Tasks tasks;

		@Option(required = true, names = {"-g", "--group"}, completionCandidates = GroupCompleter.class)
		private String group;

		@ArgGroup(exclusive = false, multiplicity = "1")
		SetListCommand.SetListArgs args;

		private static class SetGroupArgs {
			@ArgGroup(exclusive = false)
			private ProjectFeature projectFeature;

			@Option(names = {"--in-progress"})
			private boolean in_progress;
		}

		SetGroupCommand(Tasks tasks) {
			this.tasks = tasks;
		}

		@Override
		public void run() {
			if (args.projectFeature != null) {
				handleProjectAndFeature();
			}

			if (args.in_progress) {
				TaskGroup group = tasks.getGroup(this.group);

				if (group.getState() == TaskContainerState.Finished) {
					tasks.setGroupState(group, TaskContainerState.InProgress, true);

					System.out.println("Set state of group '" + group.getFullPath() + "' to In Progress");
				}
				else {
					System.out.println("Group '" + group.getFullPath() + "' must be finished first");
				}
			}

			System.out.println();
		}

		private void handleProjectAndFeature() {
			if (args.projectFeature.project != null) {
				TaskGroup group = tasks.getGroup(this.group);
				String project = this.args.projectFeature.project;
				tasks.setProject(group, project, true);

				System.out.println("Set project for group '" + group.getFullPath() + "' to '" + project + "'");
			}

			if (args.projectFeature.feature != null) {
				TaskGroup group = tasks.getGroup(this.group);
				String feature = this.args.projectFeature.feature;
				tasks.setFeature(group, feature, true);

				System.out.println("Set feature for group '" + group.getFullPath() + "' to '" + feature + "'");
			}
		}
	}

	static final class ProjectFeature {
		@Option(names = {"-p", "--project"})
		private String project;

		@Option(names = {"-f", "--feature"})
		private String feature;
	}
}
