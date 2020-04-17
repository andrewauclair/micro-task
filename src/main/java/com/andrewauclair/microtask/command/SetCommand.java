// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.jline.GroupCompleter;
import com.andrewauclair.microtask.jline.ListCompleter;
import com.andrewauclair.microtask.task.*;
import com.andrewauclair.microtask.task.group.name.ExistingTaskGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingTaskListName;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

abstract class SetCommand implements Runnable {
	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	static final class SetTaskCommand extends SetCommand {
		private final Tasks tasks;

		@Option(required = true, names = {"--task"}, description = "Task to set.")
		private ExistingID id;

		@Option(names = {"-r", "--recurring"}, description = "Set task to recurring.")
		private Boolean recurring;

		@Option(names = {"--not-recurring"}, description = "Set task to non-recurring.")
		private Boolean not_recurring;

		@Option(names = {"--inactive"}, description = "Set task state to inactive.")
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

		@Option(required = true, names = {"-l", "--list"}, completionCandidates = ListCompleter.class, description = "The list to set.")
		private ExistingTaskListName list;

		@ArgGroup(exclusive = false, multiplicity = "1")
		SetListArgs args;

		private static class SetListArgs {
			@ArgGroup(exclusive = false)
			private ProjectFeature projectFeature;

			@Option(names = {"--in-progress"}, description = "Set the list state to in progress.")
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
					tasks.setListState(this.list, TaskContainerState.InProgress, true);

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
				String project = this.args.projectFeature.project;
				tasks.setProject(list, project, true);

				System.out.println("Set project for list '" + list + "' to '" + project + "'");
			}

			if (args.projectFeature.feature != null) {
				String feature = this.args.projectFeature.feature;
				tasks.setFeature(list, feature, true);

				System.out.println("Set feature for list '" + list + "' to '" + feature + "'");
			}
		}
	}

	static final class SetGroupCommand extends SetCommand {
		private final Tasks tasks;

		@Option(required = true, names = {"-g", "--group"}, completionCandidates = GroupCompleter.class, description = "The group to set.")
		private ExistingTaskGroupName group;

		@ArgGroup(exclusive = false, multiplicity = "1")
		SetListCommand.SetListArgs args;

		private static class SetGroupArgs {
			@ArgGroup(exclusive = false)
			private ProjectFeature projectFeature;

			@Option(names = {"--in-progress"}, description = "Set the group state to in progress.")
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
				TaskGroup group = tasks.getGroup(this.group.absoluteName());

				if (group.getState() == TaskContainerState.Finished) {
					tasks.setGroupState(this.group, TaskContainerState.InProgress, true);

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
				String project = this.args.projectFeature.project;
				tasks.setProject(group, project, true);

				System.out.println("Set project for group '" + group + "' to '" + project + "'");
			}

			if (args.projectFeature.feature != null) {
				String feature = this.args.projectFeature.feature;
				tasks.setFeature(group, feature, true);

				System.out.println("Set feature for group '" + group + "' to '" + feature + "'");
			}
		}
	}

	static final class ProjectFeature {
		@Option(names = {"-p", "--project"}, description = "The project to set.")
		private String project;

		@Option(names = {"-f", "--feature"}, description = "The feature to set.")
		private String feature;
	}
}
