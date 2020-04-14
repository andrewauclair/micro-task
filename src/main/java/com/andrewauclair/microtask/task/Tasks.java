// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.Utils;
import com.andrewauclair.microtask.command.Commands;
import com.andrewauclair.microtask.os.ConsoleColors;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.group.TaskGroupFileWriter;
import com.andrewauclair.microtask.task.group.name.ExistingTaskGroupName;
import com.andrewauclair.microtask.task.list.TaskListFileWriter;
import com.andrewauclair.microtask.task.list.name.ExistingTaskListName;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import static com.andrewauclair.microtask.task.TaskGroup.ROOT_PATH;

// TODO Maybe this file becomes TasksData and really only contains the rootGroup, activeGroup, activeTaskID, activeList and nextID variables
// TODO Find good names for the classes that will take over most of the methods in this class, there might be a few of them
// TODO If we split the classes up enough, we could maybe add a list and group subpackage for task where we store those classes

@SuppressWarnings("CanBeFinal")
public class Tasks {
	public static final int NO_ACTIVE_TASK = -1;

	final OSInterface osInterface;
	private final PrintStream output;
	private final TaskWriter writer;

	private TaskGroup rootGroup = TaskGroupBuilder.createRootGroup();

	private TaskGroup activeGroup = rootGroup;
	private long activeTaskID = NO_ACTIVE_TASK;
	private String activeList = "/default";

	private long nextID = 1;

	private TaskFilterBuilder filterBuilder = new TaskFilterBuilder();

	public Tasks(TaskWriter writer, PrintStream output, OSInterface osInterface) {
		this.writer = writer;
		this.output = output;
		this.osInterface = osInterface;

		if (hasRepo()) {
			createNewRepo(osInterface);
		}
	}

	private boolean hasRepo() {
		return !osInterface.fileExists("git-data");
	}

	private void createNewRepo(OSInterface osInterface) {
		String username = osInterface.getEnvVar("username");

		osInterface.createFolder("git-data");
		osInterface.runGitCommand("git init");
		osInterface.runGitCommand("git config user.name \"" + username + "\"");
		osInterface.runGitCommand("git config user.email \"" + username + "@" + osInterface.getEnvVar("computername") + "\"");

		Utils.writeCurrentVersion(osInterface);
		writeNextID();

		new GitHelper(osInterface)
				.commit("Created new micro task instance.");

		addList("default", true);
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
		return addTask(task, activeList);
	}

	public Task addTask(String task, String list) {
		return getList(new ExistingTaskListName(this, list)).addTask(incrementID(), task);
	}

	TaskList getList(ExistingTaskListName listName) {
		return getGroupForList(listName).getListAbsolute(listName.absoluteName());
	}

	private long incrementID() {
		long nextID = this.nextID++;

		writeNextID();
		osInterface.runGitCommand("git add next-id.txt");

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

	public String getAbsoluteListName(String name) {
		if (!name.startsWith(ROOT_PATH)) {
			return activeGroup.getFullPath() + name;
		}
		return name;
	}

	// TODO Remove this, we still need it right now for hasListWithName
	private TaskGroup groupForList(String name) {
		TaskListName groupName = new TaskListName(this, name);

		TaskGroup group = getGroup(groupName.parentGroupName());

		if (!group.containsListAbsolute(name)) {
			throw new TaskException("List '" + name + "' does not exist.");
		}
		return group;
	}

	public TaskGroup getGroupForList(ExistingTaskListName list) {
		TaskGroup group = getGroup(list.parentGroupName());

		if (!group.containsListAbsolute(list.absoluteName())) {
			throw new TaskException("List '" + list.absoluteName() + "' does not exist.");
		}
		return group;
	}

	public TaskGroup getGroup(String name) {
		TaskGroupName groupName = new TaskGroupName(this, name);

		Optional<TaskGroup> optionalGroup = rootGroup.getGroupAbsolute(groupName.absoluteName());

		if (optionalGroup.isEmpty()) {
			throw new TaskException("Group '" + groupName + "' does not exist.");
		}
		return optionalGroup.get();
	}

	public String getAbsoluteGroupName(String name) {
		if (!name.startsWith("/")) {
			return activeGroup.getFullPath() + name;
		}
		return name;
	}

	public long nextID() {
		return nextID;
	}

	public void moveList(ExistingTaskListName list, ExistingTaskGroupName group) {
		TaskList currentList = getList(list);
		TaskList newList = getGroupForList(list).moveList(currentList, getGroup(group.absoluteName()), output, osInterface);

		if (activeList.equals(currentList.getFullPath())) {
			activeList = newList.getFullPath();
		}
	}

	public void moveGroup(String group, String destGroup) {
		TaskGroup groupToMove = getGroup(group);

		TaskGroup newGroup = getGroup(groupToMove.getParent()).moveGroup(groupToMove, getGroup(destGroup), output, osInterface);

		if (activeGroup.getFullPath().equals(groupToMove.getFullPath())) {
			activeGroup = newGroup;
		}
	}

	public Task moveTask(long id, ExistingTaskListName listName) {
		return getListForTask(id).moveTask(id, getList(listName));
	}

	public TaskList getListForTask(long id) {
		TaskFinder finder = new TaskFinder(this);
		if (!finder.hasTaskWithID(id)) {
			throw new TaskException("Task " + id + " does not exist.");
		}
		return findListForTask(id);
	}

	public Task renameTask(long id, String task) {
		return getListForTask(id).renameTask(id, task);
	}

	public boolean addList(String name, boolean createFiles) {
		TaskGroup group;

		TaskListName listName = new TaskListName(this, name);
		String absoluteList = listName.absoluteName();

		String groupName = listName.parentGroupName();

		// create any groups in the path that don't exist
		createGroup(groupName, createFiles);

		group = getGroup(groupName);

		if (group.getState() == TaskContainerState.Finished && createFiles) {
			throw new TaskException("List '" + absoluteList + "' cannot be created because group '" + group.getFullPath() + "' has been finished.");
		}

		if (group.containsListAbsolute(absoluteList)) {
			// TODO This mean the list already exists, should we show a message for that?
			return false;
		}

		TaskList newList = new TaskList(listName.shortName(), group, osInterface, writer, "", "", TaskContainerState.InProgress);

		group.addChild(newList);

		osInterface.createFolder("git-data/tasks" + newList.getFullPath());

		if (createFiles) {
			new TaskListFileWriter(newList, osInterface).write();

			new GitHelper(osInterface)
					.commit("Created list '" + newList.getFullPath() + "'");
		}

		return true;
	}

	public boolean hasActiveTask() {
		return activeTaskID != NO_ACTIVE_TASK;
	}

	public Task finishTask() {
		return finishTask(getActiveTask().id);
	}

	public List<Task> getTasksForList(String listName) {
		return getList(new ExistingTaskListName(this, listName)).getTasks();
	}

	public TaskList getListByName(String name) {
		return getList(new ExistingTaskListName(this, name));
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

	public Task startTask(long id, boolean finishActive) {
		Task currentTask = getListForTask(id).getTask(id);

		if (currentTask.state == TaskState.Finished) {
			throw new TaskException("Task has already been finished.");
		}

		if (activeTaskID == currentTask.id) {
			throw new TaskException("Task is already active.");
		}

		Optional<Task> lastTask = Optional.empty();

		if (activeTaskID != NO_ACTIVE_TASK) {
			if (finishActive) {
				lastTask = Optional.of(finishTask());
			}
			else {
				lastTask = Optional.of(stopTask());
			}
		}
		activeTaskID = currentTask.id;

		setActiveList(getActiveTaskList());
		setActiveGroup(getActiveTaskList().parentGroupName());

		long startTime = osInterface.currentSeconds();

		if (lastTask.isPresent()) {
			int size = lastTask.get().getStartStopTimes().size();
			startTime = lastTask.get().getStartStopTimes().get(size - 1).stop;
		}

		return getList(getActiveTaskList()).startTask(activeTaskID, startTime, this);
	}

	public Task stopTask() {
		Task stoppedTask = getList(getActiveTaskList()).stopTask(activeTaskID);

		activeTaskID = NO_ACTIVE_TASK;
		return stoppedTask;
	}

	public List<Task> getTasks() {
		return getList(new ExistingTaskListName(this, activeList)).getTasks();
	}

	public ExistingTaskListName getActiveTaskList() {
		if (activeTaskID == NO_ACTIVE_TASK) {
			throw new TaskException("No active task.");
		}
		return new ExistingTaskListName(this, getListForTask(activeTaskID).getFullPath());
	}

	public Task finishTask(long id) {
		Task task = getListForTask(id).finishTask(id);

		if (id == activeTaskID) {
			activeTaskID = NO_ACTIVE_TASK;
		}

		return task;
	}

	public Task getActiveTask() {
		if (activeTaskID == NO_ACTIVE_TASK) {
			throw new TaskException("No active task.");
		}
		TaskList list = findListForTask(activeTaskID);

		return list.getTask(activeTaskID);
	}

	public void renameList(String oldName, String newName) {
		String absoluteOldList = getAbsoluteListName(oldName);
		String absoluteNewList = getAbsoluteListName(newName);

		TaskGroup group = getGroupForList(new ExistingTaskListName(this, absoluteOldList));

		if (!group.containsListAbsolute(absoluteOldList)) {
			throw new TaskException("List '" + absoluteOldList + "' does not exist.");
		}

		if (activeList.equals(absoluteOldList)) {
			activeList = absoluteNewList;
		}

		TaskList oldList = group.getListAbsolute(absoluteOldList);

		if (oldList.getState() == TaskContainerState.Finished) {
			throw new TaskException("List '" + oldList.getFullPath() + "' has been finished and cannot be renamed.");
		}

		group.removeChild(oldList);
		group.addChild(oldList.rename(newName));

		try {
			osInterface.moveFolder(absoluteOldList, absoluteNewList);
		}
		catch (IOException e) {
			e.printStackTrace(output);
			return;
		}

		new GitHelper(osInterface)
				.commit("Renamed list '" + absoluteOldList + "' to '" + absoluteNewList + "'");
	}

	public void renameGroup(String oldName, String newName) {
		oldName = getAbsoluteGroupName(oldName);
		newName = getAbsoluteGroupName(newName);

		String oldFolder = oldName;
		String newFolder = newName;

		// TODO This isn't very nice to look at
		newName = newName.substring(newName.substring(0, newName.length() - 2).lastIndexOf('/') + 1);
		newName = newName.substring(0, newName.length() - 1);

		TaskGroup group = getGroup(oldName);

		boolean isActiveGroup = activeGroup.equals(group);

		if (group.getState() == TaskContainerState.Finished) {
			throw new TaskException("Group '" + group.getFullPath() + "' has been finished and cannot be renamed.");
		}

		TaskGroup parent = getGroup(group.getParent());
		TaskGroup newGroup = group.rename(newName);

		parent.removeChild(group);
		parent.addChild(newGroup);

		try {
			osInterface.moveFolder(oldFolder, newFolder);
		}
		catch (IOException e) {
			e.printStackTrace(output);
		}

		if (isActiveGroup) {
			activeGroup = newGroup;
		}
	}

	public void addTask(Task task) {
		TaskFinder finder = new TaskFinder(this);
		if (finder.hasTaskWithID(task.id)) {
			throw new TaskException("Task with ID " + task.id + " already exists.");
		}

		getList(new ExistingTaskListName(this, activeList)).addTask(task);

		// used to set the active task when reloading from the files
		if (task.state == TaskState.Active) {
			activeTaskID = task.id;
		}
	}

	public String getActiveList() {
		return activeList;
	}

	public void setActiveList(ExistingTaskListName listName) {
		TaskList list = getList(listName);

		activeList = list.getFullPath();
	}

	public TaskGroup addGroup(String groupName) {
		return createGroup(groupName, false);
	}

	TaskGroup createGroup(String groupName, boolean createFiles) {
		if (!groupName.startsWith(ROOT_PATH)) {
			groupName = activeGroup.getFullPath() + groupName;
		}

		if (hasGroupPath(groupName)) {
			return getGroup(groupName);
		}

		String currentParent = ROOT_PATH;
		TaskGroup newGroup = null;

		for (String group : groupName.substring(1).split("/")) {
			// TODO I think this should be an invalid path
			if (group.isEmpty()) {
				continue;
			}
			TaskGroup parentGroup = getGroup(currentParent);

			if (parentGroup.getState() == TaskContainerState.Finished && createFiles) {
				throw new TaskException("Group '" + groupName + "' cannot be created because group '" + parentGroup.getFullPath() + "' has been finished.");
			}

			newGroup = new TaskGroup(group, parentGroup, "", "", TaskContainerState.InProgress);
			currentParent = newGroup.getFullPath();

			if (!parentGroup.containsGroup(newGroup)) {
				if (createFiles) {
					new TaskGroupFileWriter(newGroup, osInterface).write();
				}
				parentGroup.addChild(newGroup);
			}
		}

		if (createFiles) {
			new GitHelper(osInterface)
					.commit("Created group '" + groupName + "'");
		}

		return newGroup;
	}

	public boolean hasGroupPath(String groupName) {
		if (!groupName.startsWith("/")) {
			throw new TaskException("Group path must start with root (/).");
		}
		return rootGroup.getGroupAbsolute(groupName).isPresent();
	}

	public TaskGroup createGroup(String groupName) {
		return createGroup(groupName, true);
	}

	public long getActiveTaskID() {
		return activeTaskID;
	}

	// TODO Could things like this be static somewhere and take a reference to the task data?
	public Task setRecurring(long id, boolean recurring) {
		Task optionalTask = getTask(id);

		if (optionalTask.state == TaskState.Finished) {
			throw new TaskException("Cannot set task " + id + " recurring state. The task has been finished.");
		}

		Task task = new TaskBuilder(optionalTask)
				.withRecurring(recurring)
				.build();

		String list = findListForTask(task.id).getFullPath();
		replaceTask(list, optionalTask, task);

		writer.writeTask(task, "git-data/tasks" + list + "/" + task.id + ".txt");

		new GitHelper(osInterface)
				.withFile("tasks" + list + "/" + task.id + ".txt")
				.commit("Set recurring for task " + task.id + " to " + recurring);

		return task;
	}

	public Task getTask(long id) {
		Optional<Task> optionalTask = getAllTasks().stream()
				.filter(task -> task.id == id)
				.findFirst();

		if (optionalTask.isEmpty()) {
			throw new TaskException("Task " + id + " does not exist.");
		}
		return optionalTask.get();
	}

	public TaskList findListForTask(long id) {
		Optional<TaskList> listForTask = rootGroup.findListForTask(id);
		if (listForTask.isEmpty()) {
			throw new TaskException("List for task " + id + " was not found.");
		}
		return listForTask.get();
	}

	private void replaceTask(String listName, Task oldTask, Task newTask) {
		TaskList list = getList(new ExistingTaskListName(this, listName));
		list.removeTask(oldTask);
		list.addTask(newTask);
	}

	public List<Task> getAllTasks() {
		return rootGroup.getTasks();
	}

	public Task setTaskState(long id, TaskState state) {
		Task optionalTask = getTask(id);

		Task task = new TaskBuilder(optionalTask)
				.withState(state)
				.build();

		String list = findListForTask(task.id).getFullPath();
		replaceTask(list, optionalTask, task);

		writer.writeTask(task, "git-data/tasks" + list + "/" + task.id + ".txt");

		new GitHelper(osInterface)
				.withFile("tasks" + list + "/" + task.id + ".txt")
				.commit("Set state for task " + task.id + " to " + state);

		return task;
	}

	public TaskList setProject(TaskList list, String project, boolean createFiles) {
		if (list.getState() == TaskContainerState.Finished) {
			throw new TaskException("Cannot set project on list '" + list.getFullPath() + "' because it has been finished.");
		}

		String listName = list.getFullPath();

		TaskGroup group = getGroupForList(new ExistingTaskListName(this, listName));

		group.removeChild(list);

		TaskList newList = list.changeProject(project);
		group.addChild(newList);

		if (createFiles) {
			new TaskListFileWriter(newList, osInterface).write();

			new GitHelper(osInterface)
					.commit("Set project for list '" + listName + "' to '" + project + "'");
		}

		return newList;
	}

	public void setProject(TaskGroup group, String project, boolean createFiles) {
		if (group.getState() == TaskContainerState.Finished) {
			throw new TaskException("Cannot set project on group '" + group.getFullPath() + "' because it has been finished.");
		}

		TaskGroup newGroup;
		if (group == rootGroup) {
			rootGroup = group.changeProject(project);
			newGroup = rootGroup;
		}
		else {
			TaskGroup parent = getGroup(group.getParent());

			parent.removeChild(group);

			newGroup = group.changeProject(project);
			parent.addChild(newGroup);
		}

		if (createFiles) {
			new TaskGroupFileWriter(newGroup, osInterface).write();

			new GitHelper(osInterface)
					.commit("Set project for group '" + group.getFullPath() + "' to '" + project + "'");
		}
	}

	public TaskList setFeature(TaskList list, String feature, boolean createFiles) {
		if (list.getState() == TaskContainerState.Finished) {
			throw new TaskException("Cannot set feature on list '" + list.getFullPath() + "' because it has been finished.");
		}

		String listName = list.getFullPath();

		TaskGroup group = getGroupForList(new ExistingTaskListName(this, listName));

		group.removeChild(list);

		TaskList newList = list.changeFeature(feature);
		group.addChild(newList);

		if (createFiles) {
			new TaskListFileWriter(newList, osInterface).write();

			new GitHelper(osInterface)
					.commit("Set feature for list '" + listName + "' to '" + feature + "'");
		}

		return newList;
	}

	public void setFeature(TaskGroup group, String feature, boolean createFiles) {
		if (group.getState() == TaskContainerState.Finished) {
			throw new TaskException("Cannot set feature on group '" + group.getFullPath() + "' because it has been finished.");
		}

		TaskGroup newGroup;
		if (group == rootGroup) {
			rootGroup = group.changeFeature(feature);
			newGroup = rootGroup;
		}
		else {
			TaskGroup parent = getGroup(group.getParent());

			parent.removeChild(group);

			newGroup = group.changeFeature(feature);
			parent.addChild(newGroup);
		}

		if (createFiles) {
			new TaskGroupFileWriter(newGroup, osInterface).write();

			new GitHelper(osInterface)
					.commit("Set feature for group '" + newGroup.getFullPath() + "' to '" + feature + "'");
		}
	}

	public void setGroupState(TaskGroup group, TaskContainerState state, boolean createFiles) {
		TaskGroup parent = getGroup(group.getParent());

		parent.removeChild(group);

		TaskGroup taskGroup = group.changeState(state);

		parent.addChild(taskGroup);

		if (createFiles) {
			new TaskGroupFileWriter(taskGroup, osInterface).write();

			new GitHelper(osInterface)
					.commit("Set state for group '" + taskGroup.getFullPath() + "' to " + state);
		}
	}

	public void setListState(TaskList list, TaskContainerState state, boolean createFiles) {
		TaskGroup parent = getGroupForList(new ExistingTaskListName(this, list.getFullPath()));

		parent.removeChild(list);

		TaskList newList = list.changeState(state);

		parent.addChild(newList);

		if (createFiles) {
			new TaskListFileWriter(newList, osInterface).write();

			new GitHelper(osInterface)
					.commit("Set state for list '" + newList.getFullPath() + "' to " + state);
		}
	}

	public TaskGroup setActiveGroup(String groupName) {
		activeGroup = getGroup(groupName);

		return activeGroup;
	}

	public TaskGroup getActiveGroup() {
		return activeGroup;
	}

	public TaskGroup getRootGroup() {
		return rootGroup;
	}

	public TaskList finishList(String list) {
		TaskList taskList = getList(new ExistingTaskListName(this, list));

		TaskGroup parent = getGroupForList(new ExistingTaskListName(this, taskList.getFullPath()));

		TaskList newList = taskList.changeState(TaskContainerState.Finished);

		parent.removeChild(taskList);
		parent.addChild(newList);

		new TaskListFileWriter(newList, osInterface).write();

		new GitHelper(osInterface)
				.withFile("tasks" + newList.getFullPath() + "/list.txt")
				.commit("Finished list '" + newList.getFullPath() + "'");

		return newList;
	}

	public TaskGroup finishGroup(String group) {

		TaskGroup origGroup = getGroup(group);
		TaskGroup parent = getGroup(origGroup.getParent());

		TaskGroup taskGroup = origGroup.changeState(TaskContainerState.Finished);

		parent.removeChild(origGroup);
		parent.addChild(taskGroup);

		new TaskGroupFileWriter(taskGroup, osInterface).write();

		new GitHelper(osInterface)
				.withFile("tasks" + taskGroup.getFullPath() + "group.txt")
				.commit("Finished group '" + taskGroup.getFullPath() + "'");

		return taskGroup;
	}

	public boolean load(TaskLoader loader, Commands commands) {
		rootGroup = TaskGroupBuilder.createRootGroup();
		activeGroup = rootGroup;
		activeTaskID = NO_ACTIVE_TASK;

		try {
			nextID = getStartingID();
			loader.load();
			commands.loadAliases();

			Optional<Task> activeTask = getAllTasks().stream()
					.filter(task -> task.state == TaskState.Active)
					.findFirst();

			if (activeTask.isPresent()) {
				activeTaskID = activeTask.get().id;
				activeList = findListForTask(activeTaskID).getFullPath();
				activeGroup = getGroupForList(new ExistingTaskListName(this, activeList));
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
