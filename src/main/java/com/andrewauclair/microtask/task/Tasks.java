// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.command.Commands;
import com.andrewauclair.microtask.os.ConsoleColors;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.project.Projects;
import com.andrewauclair.microtask.task.add.ListAdder;
import com.andrewauclair.microtask.task.build.TaskBuilder;
import com.andrewauclair.microtask.task.group.TaskGroupFileWriter;
import com.andrewauclair.microtask.task.group.name.ExistingGroupName;
import com.andrewauclair.microtask.task.group.name.NewTaskGroupName;
import com.andrewauclair.microtask.task.list.TaskListFileWriter;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import com.andrewauclair.microtask.task.list.name.NewTaskListName;
import com.andrewauclair.microtask.task.move.GroupMover;
import com.andrewauclair.microtask.task.move.ListMover;
import com.andrewauclair.microtask.task.update.TaskRecurringUpdater;
import com.andrewauclair.microtask.task.update.TaskStateUpdater;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import static com.andrewauclair.microtask.task.ActiveContext.NO_ACTIVE_TASK;
import static com.andrewauclair.microtask.task.TaskGroup.ROOT_PATH;

// TODO Maybe this file becomes TasksData and really only contains the rootGroup, activeGroup, activeTaskID, activeList and nextID variables
// TODO Find good names for the classes that will take over most of the methods in this class, there might be a few of them
// TODO If we split the classes up enough, we could maybe add a list and group subpackage for task where we store those classes

@SuppressWarnings("CanBeFinal")
public class Tasks {
	final OSInterface osInterface;
	private final PrintStream output;
	private final TaskWriter writer;

	private Projects projects;

	private TaskGroup rootGroup = TaskGroupBuilder.createRootGroup();

	private final ActiveContext activeContext = new ActiveContext(this);

	private long nextID = 1;

	private TaskFilterBuilder filterBuilder = new TaskFilterBuilder();

	public Tasks(TaskWriter writer, PrintStream output, OSInterface osInterface) {
		this.writer = writer;
		this.output = output;
		this.osInterface = osInterface;

		if (!new TaskListFinder(this).hasList(new TaskListName(this, "/default") {})) {
			addList(new NewTaskListName(this, "/default"), osInterface.canCreateFiles());
		}
		activeContext.setCurrentList(new ExistingListName(this, "/default"));
	}

	public void setProjects(Projects projects) {
		this.projects = projects;
	}
	public TaskFilterBuilder getFilterBuilder() {
		return filterBuilder;
	}

	public void setFilterBuilder(TaskFilterBuilder filterBuilder) {
		this.filterBuilder = filterBuilder;
	}

	public TaskWriter getWriter() {
		return writer;
	}

	// TODO This is really only used in the tests, maybe we shouldn't have it
	// TODO I'd like to convert as many tests as possible over to using the /default list, they don't need to be making so many lists
	public Task addTask(String task) {
		return addTask(task, activeContext.getCurrentList());
	}

	public Task addTask(String task, ExistingListName list) {
		// TODO This wastes an ID if the list or group is finished and we don't actually add the task
		return getList(list).addTask(new NewID(this, incrementID()), task);
	}

	public TaskList getList(ExistingListName listName) {
		return getGroupForList(listName).getListAbsolute(listName.absoluteName());
	}

	private long incrementID() {
		long nextID = this.nextID++;

		writeNextID();
//		osInterface.runGitCommand("git add next-id.txt");

		return nextID;
	}

	private void writeNextID() {
		try (PrintStream outputStream = new PrintStream(osInterface.createOutputStream("git-data/next-id.txt"))) {
			outputStream.print(this.nextID);
		}
		catch (IOException e) {
			e.printStackTrace(output);
		}
	}

	public TaskGroup getGroupForList(ExistingListName list) {
		TaskGroup group = getGroup(list.parentGroupName());

		if (!group.containsListAbsolute(list.absoluteName())) {
			throw new TaskException("List '" + list.absoluteName() + "' does not exist.");
		}
		return group;
	}

	public TaskGroup getGroup(ExistingGroupName group) {
		return getGroup(group.absoluteName());
	}

	public TaskGroup getGroup(String name) {
		TaskGroupName groupName = new TaskGroupName(this, name);

		Optional<TaskGroup> optionalGroup = rootGroup.getGroupAbsolute(groupName.absoluteName());

		if (optionalGroup.isEmpty()) {
			throw new TaskException("Group '" + groupName + "' does not exist.");
		}
		return optionalGroup.get();
	}

	public long nextID() {
		return nextID;
	}

	public void moveList(ExistingListName list, ExistingGroupName group) {
		ListMover mover = new ListMover(this, osInterface);
		mover.moveList(list, group);
	}

	public void moveGroup(ExistingGroupName group, ExistingGroupName destGroup) {
		GroupMover mover = new GroupMover(this, osInterface);
		mover.moveGroup(group, destGroup);
	}

	public Task moveTask(ExistingID id, ExistingListName listName) {
		return getListForTask(id).moveTask(id, getList(listName));
	}

	public TaskList getListForTask(ExistingID id) {
		return findListForTask(id);
	}

	public Task renameTask(ExistingID id, String task) {
		return getListForTask(id).renameTask(id, task);
	}

	public void addList(NewTaskListName name, boolean createFiles) {
		ListAdder adder = new ListAdder(this, writer, osInterface);
		adder.addList(name, createFiles);
	}

	public boolean hasActiveTask() {
		return activeContext.getActiveTaskID() != NO_ACTIVE_TASK;
	}

	public Task finishTask() {
		return finishTask(new ExistingID(this, getActiveTask().id));
	}

	public List<Task> getTasksForList(ExistingListName listName) {
		return getList(listName).getTasks();
	}

	public TaskList getListByName(ExistingListName name) {
		return getList(name);
	}

	Set<String> getAllListNames() {
		return getLists(rootGroup).stream()
				.map(TaskList::getFullPath)
				.collect(Collectors.toSet());
	}

	public Set<String> getInProgressListNames() {
		return getLists(rootGroup).stream()
				.filter(list -> list.getState() != TaskContainerState.Finished)
				.map(TaskList::getFullPath)
				.collect(Collectors.toSet());
	}

	private List<TaskList> getLists(TaskGroup group) {
		List<TaskList> lists = group.getChildren().stream()
				.filter(child -> child instanceof TaskList)
				.map(child -> (TaskList) child)
				.collect(Collectors.toList());

		group.getChildren().stream()
				.filter(child -> child instanceof TaskGroup)
				.map(child -> (TaskGroup) child)
				.forEach(nestedGroup -> lists.addAll(getLists(nestedGroup)));

		return lists;
	}

	public Set<String> getInProgressGroupNames() {
		return getInProgressGroupNames(rootGroup);
	}

	private Set<String> getInProgressGroupNames(TaskGroup group) {
		Set<String> groups = group.getChildren().stream()
				.filter(child -> child instanceof TaskGroup)
				.filter(child -> child.getState() != TaskContainerState.Finished)
				.map(TaskContainer::getFullPath)
				.collect(Collectors.toSet());

		group.getChildren().stream()
				.filter(child -> child instanceof TaskGroup)
				.filter(child -> child.getState() != TaskContainerState.Finished)
				.map(child -> (TaskGroup) child)
				.forEach(nestedGroup -> groups.addAll(getInProgressGroupNames(nestedGroup)));

		return groups;
	}

	public Task startTask(ExistingID id, boolean finishActive) {
		TaskStateUpdater updater = new TaskStateUpdater(this, projects, osInterface);
		return updater.startTask(id, finishActive);
	}

	public Task stopTask() {
		TaskStateUpdater updater = new TaskStateUpdater(this, projects, osInterface);
		return updater.stopTask();
	}

	public List<Task> getTasks() {
		return getList(activeContext.getCurrentList()).getTasks();
	}

	public ExistingListName getActiveTaskList() {
		if (activeContext.getActiveTaskID() == NO_ACTIVE_TASK) {
			throw new TaskException("No active task.");
		}
		return new ExistingListName(this, getListForTask(new ExistingID(this, activeContext.getActiveTaskID())).getFullPath());
	}

	public Task finishTask(ExistingID id) {
		TaskStateUpdater updater = new TaskStateUpdater(this, projects, osInterface);
		return updater.finishTask(id);
	}

	public Task getActiveTask() {
		if (activeContext.getActiveTaskID() == NO_ACTIVE_TASK) {
			throw new TaskException("No active task.");
		}
		TaskList list = findListForTask(new ExistingID(this, activeContext.getActiveTaskID()));

		return list.getTask(new ExistingID(this, activeContext.getActiveTaskID()));
	}

	public void renameList(ExistingListName currentName, NewTaskListName newName) {
		TaskGroup group = getGroupForList(currentName);

		TaskList currentList = group.getListAbsolute(currentName.absoluteName());

		if (currentList.getState() == TaskContainerState.Finished) {
			throw new TaskException("List '" + currentName + "' has been finished and cannot be renamed.");
		}

		group.removeChild(currentList);
		TaskList renamedList = currentList.rename(newName.shortName());
		group.addChild(renamedList);

		if (activeContext.getCurrentList().equals(currentName)) {
			activeContext.setCurrentList(new ExistingListName(this, newName.absoluteName()));
		}

		try {
			osInterface.moveFolder(currentName.absoluteName(), newName.absoluteName());
		}
		catch (IOException e) {
			e.printStackTrace(output);
			return;
		}

		osInterface.gitCommit("Renamed list '" + currentName + "' to '" + newName + "'");
	}

	public void renameGroup(ExistingGroupName currentGroup, NewTaskGroupName newGroup) {
		TaskGroup group = getGroup(currentGroup);

		boolean isCurrentGroup = activeContext.getCurrentGroup().absoluteName().equals(group.getFullPath());

		if (group.getState() == TaskContainerState.Finished) {
			throw new TaskException("Group '" + group.getFullPath() + "' has been finished and cannot be renamed.");
		}

		TaskGroup parent = getGroup(group.getParent());
		TaskGroup newGroupChild = group.rename(newGroup.shortName());

		parent.removeChild(group);
		parent.addChild(newGroupChild);

		try {
			osInterface.moveFolder(currentGroup.absoluteName(), newGroup.absoluteName());
		}
		catch (IOException e) {
			e.printStackTrace(output);
		}

		if (isCurrentGroup) {
			activeContext.setCurrentGroup(new ExistingGroupName(this, newGroup.absoluteName()));
		}
	}

	public void addTask(Task task) {
		TaskFinder finder = new TaskFinder(this);
		if (finder.hasTaskWithID(task.id)) {
			throw new TaskException("Task with ID " + task.id + " already exists.");
		}

		getList(activeContext.getCurrentList()).addTask(task);

		// used to set the active task when reloading from the files
		if (task.state == TaskState.Active) {
			activeContext.setActiveTaskID(task.id);
		}
	}

	public ActiveContext getActiveContext() {
		return activeContext;
	}

	public ExistingListName getCurrentList() {
		return activeContext.getCurrentList();
	}

	public void setActiveTaskID(long activeTaskID) {
		activeContext.setActiveTaskID(activeTaskID);
	}

	public void setCurrentList(ExistingListName listName) {
		activeContext.setCurrentList(listName);
	}

	public TaskGroup addGroup(NewTaskGroupName groupName) {
		return createGroup(groupName, false);
	}

	public TaskGroup createGroup(TaskGroupName groupName, boolean createFiles) {
		TaskGroupFinder finder = new TaskGroupFinder(this);

		if (finder.hasGroupPath(groupName)) {
			return getGroup(new ExistingGroupName(this, groupName.absoluteName()));
		}

		String currentParent = ROOT_PATH;
		TaskGroup newGroup = null;

		for (String group : groupName.absoluteName().substring(1).split("/")) {
			TaskGroup parentGroup = getGroup(currentParent);

			if (parentGroup.getState() == TaskContainerState.Finished && createFiles) {
				throw new TaskException("Group '" + groupName + "' cannot be created because group '" + parentGroup.getFullPath() + "' has been finished.");
			}

			newGroup = new TaskGroup(group, parentGroup, TaskContainerState.InProgress);
			currentParent = newGroup.getFullPath();

			if (!parentGroup.containsGroup(newGroup)) {
				if (createFiles) {
					new TaskGroupFileWriter(newGroup, osInterface).write();
				}
				parentGroup.addChild(newGroup);
			}
		}

		if (createFiles) {
			osInterface.gitCommit("Created group '" + groupName + "'");
		}

		return newGroup;
	}

	public TaskGroup createGroup(NewTaskGroupName groupName) {
		return createGroup(groupName, true);
	}

	public long getActiveTaskID() {
		return activeContext.getActiveTaskID();
	}

	public Task setRecurring(ExistingID id, boolean recurring) {
		TaskRecurringUpdater updater = new TaskRecurringUpdater(this, osInterface);
		return updater.updateRecurring(id, recurring);
	}

	public Task setTags(ExistingID id, List<String> tags) {
		Task origTask = getTask(id);
		TaskBuilder builder = new TaskBuilder(origTask);

		tags.forEach(builder::withTag);

		Task task = builder.build();

		String list = findListForTask(new ExistingID(this, task.id)).getFullPath();
		replaceTask(new ExistingListName(this, list), origTask, task);

		String file = "git-data/tasks" + list + "/" + task.id + ".txt";
		writer.writeTask(task, file);

		osInterface.gitCommit("Set tag(s) for task " + task.id + " to " + String.join(", ", tags));

		return task;
	}

	public Task getTask(ExistingID id) {
		Optional<Task> optionalTask = getAllTasks().stream()
				.filter(task -> task.id == id.get())
				.findFirst();

		if (optionalTask.isEmpty()) {
			throw new TaskException("Task " + id.get() + " does not exist.");
		}
		return optionalTask.get();
	}

	public TaskList findListForTask(ExistingID id) {
		Optional<TaskList> listForTask = rootGroup.findListForTask(id);
		if (listForTask.isEmpty()) {
			throw new TaskException("List for task " + id + " was not found.");
		}
		return listForTask.get();
	}

	public void replaceTask(ExistingListName listName, Task oldTask, Task newTask) {
		TaskList list = getList(listName);
		list.removeTask(oldTask);
		list.addTask(newTask);
	}

	public List<Task> getAllTasks() {
		return rootGroup.getTasks();
	}

	public Task setTaskState(ExistingID id, TaskState state) {
		Task optionalTask = getTask(id);

		Task task = new TaskBuilder(optionalTask)
				.withState(state)
				.build();

		String list = findListForTask(new ExistingID(this, task.id)).getFullPath();
		replaceTask(new ExistingListName(this, list), optionalTask, task);

		String file = "git-data/tasks" + list + "/" + task.id + ".txt";
		writer.writeTask(task, file);

		osInterface.gitCommit("Set state for task " + task.id + " to " + state);

		return task;
	}

	public void setGroupState(ExistingGroupName groupName, TaskContainerState state, boolean createFiles) {
		TaskGroup group = getGroup(groupName);

		TaskGroup parent = getGroup(group.getParent());

		parent.removeChild(group);

		TaskGroup taskGroup = group.changeState(state);

		parent.addChild(taskGroup);

		if (createFiles) {
			new TaskGroupFileWriter(taskGroup, osInterface).write();

			osInterface.gitCommit("Set state for group '" + taskGroup.getFullPath() + "' to " + state);
		}
	}

	public void setListState(ExistingListName listName, TaskContainerState state, boolean createFiles) {
		TaskList list = getList(listName);

		TaskGroup parent = getGroupForList(new ExistingListName(this, list.getFullPath()));

		parent.removeChild(list);

		TaskList newList = list.changeState(state);

		parent.addChild(newList);

		if (createFiles) {
			new TaskListFileWriter(newList, osInterface).write();

			osInterface.gitCommit("Set state for list '" + newList.getFullPath() + "' to " + state);
		}
	}

	public TaskGroup setCurrentGroup(ExistingGroupName groupName) {
		activeContext.setCurrentGroup(groupName);

		return getGroup(activeContext.getCurrentGroup());
	}

	public TaskGroup getCurrentGroup() {
		return getGroup(activeContext.getCurrentGroup());
	}

	public TaskGroup getRootGroup() {
		return rootGroup;
	}

	public TaskList finishList(ExistingListName list) {
		TaskList taskList = getList(list);

		TaskGroup parent = getGroupForList(list);

		if (taskList.getState() == TaskContainerState.Finished) {
			throw new TaskException("List has already been finished.");
		}

		TaskList newList = taskList.changeState(TaskContainerState.Finished);

		parent.removeChild(taskList);
		parent.addChild(newList);

		new TaskListFileWriter(newList, osInterface).write();

		osInterface.gitCommit("Finished list '" + newList.getFullPath() + "'");

		return newList;
	}

	public TaskGroup finishGroup(ExistingGroupName group) {
		TaskGroup origGroup = getGroup(group);
		TaskGroup parent = getGroup(origGroup.getParent());

		if (origGroup.getState() == TaskContainerState.Finished) {
			throw new TaskException("Group has already been finished.");
		}

		TaskGroup taskGroup = origGroup.changeState(TaskContainerState.Finished);

		parent.removeChild(origGroup);
		parent.addChild(taskGroup);

		new TaskGroupFileWriter(taskGroup, osInterface).write();

		osInterface.gitCommit("Finished group '" + taskGroup.getFullPath() + "'");

		return taskGroup;
	}

	public boolean load(DataLoader loader, Commands commands) {
		rootGroup = TaskGroupBuilder.createRootGroup();
		activeContext.setCurrentGroup(new ExistingGroupName(this, ROOT_PATH));
		activeContext.setActiveTaskID(NO_ACTIVE_TASK);

		try {
			nextID = getStartingID();
			loader.load();
			commands.loadAliases();

			Optional<Task> activeTask = getAllTasks().stream()
					.filter(task -> task.state == TaskState.Active)
					.findFirst();

			if (activeTask.isPresent()) {
				activeContext.setActiveTaskID(activeTask.get().id);
				activeContext.setCurrentList(new ExistingListName(this, findListForTask(new ExistingID(this, activeContext.getActiveTaskID())).getFullPath()));
				activeContext.setCurrentGroup(new ExistingGroupName(this, getGroupForList(activeContext.getCurrentList()).getFullPath()));
			}
		}
		catch (Exception e) {
			System.out.println(ConsoleColors.ConsoleForegroundColor.ANSI_FG_RED + "Failed to read tasks." + ConsoleColors.ANSI_RESET);
			e.printStackTrace(System.out);

			System.out.println();
			System.out.println("Last file: " + osInterface.getLastInputFile());

			return false;
		}

		return true;
	}

	private long getStartingID() {
		try (InputStream inputStream = osInterface.createInputStream("git-data/next-id.txt")) {
			Scanner scanner = new Scanner(inputStream);
			return scanner.nextLong();
		}
		catch (Exception ignored) {
		}
		return 1;
	}
}
