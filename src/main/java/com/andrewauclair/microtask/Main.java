// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask;

import com.andrewauclair.microtask.os.MainConsole;
import com.andrewauclair.microtask.os.OSInterfaceImpl;
import com.andrewauclair.microtask.os.StatusConsole;

public final class Main {
	public static void main(String[] args) throws Exception {
		if (args.length > 0 && args[0].equals("status")) {
			OSInterfaceImpl.disableGit = true; // don't use git on the status console
			new StatusConsole();

			System.exit(0);
		}
		else {
			if (args.length > 0 && args[0].equals("--disable-git")) {
				OSInterfaceImpl.disableGit = true;
			}
			new MainConsole();
		}
	}

	// temporary function that we will use to assign a project and feature to all existing task times at work
	// I'll remove this once I can verify that everything is working well for projects and features
//	private void manualProjectFeatureAssign(Tasks tasks) {
//		TaskWriter writer = new TaskWriter(Main.osInterface);
//
//		for (Task task : tasks.getAllTasks()) {
//			List<TaskTimes> oldTimes = task.getStartStopTimes();
//			List<TaskTimes> newTimes = new ArrayList<>();
//
//			newTimes.add(task.getAllTimes().get(0));
//
//			for (TaskTimes time : oldTimes) {
//				newTimes.add(new TaskTimes(time.start, time.stop, new TaskFinder(tasks).getProjectForTask(new ExistingID(tasks, task.id)), new TaskFinder(tasks).getFeatureForTask(new ExistingID(tasks, task.id))));
//			}
//
//			Task newTask = new Task(task.id, task.task, task.state, newTimes, task.isRecurring());
//
//			writer.writeTask(newTask, "git-data/tasks/" + tasks.findListForTask(new ExistingID(tasks, task.id)).getFullPath() + "/" + task.id + ".txt");
//		}
//		System.exit(0);
//	}
//
//	// Export data with generic names, this will remove any possible proprietary data
//	// Tasks will be exported as X - 'Task X'
//	// Groups will be exported as 'group-x'
//	// Lists will be exported as 'list-x'
//	private void exportData(Tasks tasks) {
//		exportGroup(tasks, tasks.getRootGroup(), "/", 1, 1, Main.osInterface);
//
//		try (PrintStream outputStream = new PrintStream(Main.osInterface.createOutputStream("git-data-export/next-id.txt"))) {
//			outputStream.print(tasks.nextID());
//		}
//		catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//
//	private void exportGroup(Tasks tasks, TaskGroup group, String path, int groupNum, int listNum, OSInterface osInterface) {
//		new TaskGroupFileWriter(group, osInterface)
//				.inFolder("git-data-export")
//				.write();
//
//		for (TaskContainer child : group.getChildren()) {
//			if (child instanceof TaskGroup) {
//				String name = "group-" + groupNum;
//				groupNum++;
//				exportGroup(tasks, (TaskGroup) child, path + name + "/", groupNum, listNum, osInterface);
//			}
//			else if (child instanceof TaskList) {
//				String name = "list-" + listNum;
//				listNum++;
//
//				exportList(tasks, (TaskList) child, path + name, osInterface);
//			}
//		}
//	}
//
//	private void exportList(Tasks tasks, TaskList list, String path, OSInterface osInterface) {
////		tasks.writeListInfoFile(list, "git-data-export");
//
//		new TaskListFileWriter(list, osInterface)
//				.inFolder("git-data-export")
//				.write();
//
//		TaskWriter writer = new TaskWriter(osInterface);
//
//		for (Task task : list.getTasks()) {
//			Task strippedTask = new TaskBuilder(task)
//					.withName("Task " + task.id)
//					.build();
//
//			writer.writeTask(strippedTask, "git-data-export/tasks" + path + "/" + task.id + ".txt");
//		}
//	}
//
//	// TODO Find a way to test this, build it into the task loader as the last step
//	private boolean requiresTaskUpdate() {
//		String currentVersion = "";
//
//		try {
//			currentVersion = Main.osInterface.getVersion();
//		}
//		catch (IOException ignored) {
//		}
//
//		try (InputStream inputStream = Main.osInterface.createInputStream("git-data/task-data-version.txt")) {
//			Scanner scanner = new Scanner(inputStream);
//
//			String dataVersion = scanner.nextLine();
//
//			return !currentVersion.equals(dataVersion);
//		}
//		catch (Exception ignored) {
//		}
//		// if the file doesn't exist, then yes, we need to update
//		return true;
//	}
//
//
}
