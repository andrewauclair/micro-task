// Copyright (C) 2019-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.Utils;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskTimes;
import com.andrewauclair.microtask.task.Tasks;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.ArrayList;
import java.util.List;

@Command(name = "stop", description = "Stop the active task, list, group, project, feature or tags.", subcommands = {
		StopCommand.StopTaskCommand.class,
		StopCommand.StopListCommand.class,
		StopCommand.StopGroupCommand.class,
		StopCommand.StopProjectCommand.class,
		StopCommand.StopFeatureCommand.class,
		StopCommand.StopMilestoneCommand.class,
		StopCommand.StopTagsCommand.class
})
final class StopCommand implements Runnable {
	private final Tasks tasks;
	private final OSInterface osInterface;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	StopCommand(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}

	@CommandLine.Spec
	private CommandLine.Model.CommandSpec spec;

	@Override
	public void run() {
		spec.commandLine().usage(System.out);
	}

	@Command(name = "task")
	public static class StopTaskCommand implements Runnable {
		@CommandLine.ParentCommand
		private StopCommand parent;

		@Override
		public void run() {
			Task task = parent.tasks.stopTask();

			System.out.println("Stopped task " + task.description());
			System.out.println();

			List<TaskTimes> times = task.startStopTimes;
			TaskTimes stopTime = times.get(times.size() - 1);

			System.out.println(stopTime.description(parent.osInterface.getZoneId()));
			System.out.println();
			System.out.print("Task was active for: ");
			System.out.println(Utils.formatTime(stopTime.getDuration(parent.osInterface), Utils.HighestTime.None));
			System.out.println();
		}
	}

	@Command(name = "list")
	public static class StopListCommand implements Runnable {
		@CommandLine.ParentCommand
		private StopCommand parent;

		@Override
		public void run() {
			parent.tasks.getActiveContext().setNoActiveList();
		}
	}

	@Command(name = "group")
	public static class StopGroupCommand implements Runnable {
		@CommandLine.ParentCommand
		private StopCommand parent;

		@Override
		public void run() {
			parent.tasks.getActiveContext().setNoActiveGroup();
		}
	}

	@Command(name = "project")
	public static class StopProjectCommand implements Runnable {
		@CommandLine.ParentCommand
		private StopCommand parent;

		@Override
		public void run() {
			parent.tasks.getActiveContext().setNoActiveProject();
		}
	}

	@Command(name = "feature")
	public static class StopFeatureCommand implements Runnable {
		@CommandLine.ParentCommand
		private StopCommand parent;

		@Override
		public void run() {
			parent.tasks.getActiveContext().setNoActiveFeature();
		}
	}

	@Command(name = "milestone")
	public static class StopMilestoneCommand implements Runnable {
		@CommandLine.ParentCommand
		private StopCommand parent;

		@Override
		public void run() {
			parent.tasks.getActiveContext().setNoActiveMiletone();
		}
	}

	@Command(name = "tags")
	public static class StopTagsCommand implements Runnable {
		@CommandLine.ParentCommand
		private StopCommand parent;

		@CommandLine.Parameters(description = "The tag(s) to stop.")
		private List<String> tags;

		@Override
		public void run() {
			if (tags == null) {
				parent.tasks.getActiveContext().setNoActiveTags();
			}
			else {
				List<String> activeTags = new ArrayList<>(parent.tasks.getActiveContext().getActiveTags());
				activeTags.removeAll(tags);
				parent.tasks.getActiveContext().setActiveTags(activeTags);
			}
		}
	}
}
