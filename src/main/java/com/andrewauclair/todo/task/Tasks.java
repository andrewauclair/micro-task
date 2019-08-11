// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import com.andrewauclair.todo.os.OSInterface;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Tasks {
	private static final int NO_ACTIVE_TASK = -1;

	private final OSInterface osInterface;
	private final PrintStream output;
	
	// TODO I think we should get rid of this and get groups from using activeGroup and rootGroup
	private final List<TaskGroup> groups = new ArrayList<>();
	private TaskGroup activeGroup = new TaskGroup("/");
	private final TaskGroup rootGroup = activeGroup;

	private final TaskWriter writer;
	private long startingID;
	private long activeTaskID = NO_ACTIVE_TASK;

	private String currentList = "/default";
	
	// TODO Could we maybe just make a function to get this so we don't have to track it?
	private String activeTaskList = "";
	
	public Tasks(long startID, TaskWriter writer, PrintStream output, OSInterface osInterface) {
		this.startingID = startID;
		this.writer = writer;
		this.output = output;
		this.osInterface = osInterface;

		TaskList defaultList = new TaskList("/default", osInterface);

		activeGroup.addChild(defaultList);
		groups.add(activeGroup);
	}

	public TaskWriter getWriter() {
		return writer;
	}

	public Task addTask(String task) {
		return addTask(task, currentList);
	}
	
	public Task addTask(String task, String list) {
		return getList(list).addTask(incrementID(), task, writer);
	}
	
	private long incrementID() {
		long nextID = startingID++;

		// TODO I'm not sure I'm verifying anywhere that I'm writing the correct value to this file
		try (OutputStream outputStream = osInterface.createOutputStream("git-data/next-id.txt")) {
			outputStream.write(String.valueOf(startingID).getBytes());
		}
		catch (IOException e) {
			e.printStackTrace(output);
		}
		osInterface.runGitCommand("git add next-id.txt");
		
		return nextID;
	}
	
	// TODO This should mostly be moved into TaskList
	public Task moveTask(long id, String list) {
		String absoluteList = getAbsoluteListName(list);
		
		String listForTask = findListForTask(id);
		Optional<Task> optionalTask = getTask(id, listForTask);
		
		if (optionalTask.isPresent()) {
			Task task = optionalTask.get();
			
			if (!getListNames().contains(absoluteList)) {
				throw new RuntimeException("List '" + absoluteList + "' was not found.");
			}
			
			getList(listForTask).removeTask(task);
			getList(absoluteList).addTask(task);
			
			if (task.state == TaskState.Active) {
				activeTaskList = absoluteList;
			}
			
			osInterface.removeFile("git-data/tasks" + listForTask + "/" + task.id + ".txt");
			
			writeTask(task, absoluteList);
			osInterface.runGitCommand("git add tasks" + listForTask + "/" + task.id + ".txt");
			osInterface.runGitCommand("git add tasks" + absoluteList + "/" + task.id + ".txt");
			osInterface.runGitCommand("git commit -m \"Moved task " + task.description().replace("\"", "\\\"") + " to list '" + absoluteList + "'\"");
			
			return optionalTask.get();
		}
		throw new RuntimeException("Task " + id + " was not found.");
	}

	// TODO This should probably be Optional<String>, returning the current list when we couldn't find the task is odd
	public String findListForTask(long id) {
		Optional<String> listForTask = rootGroup.findListForTask(id);
		if (!listForTask.isPresent()) {
//			throw new RuntimeException("Couldn't find list for task: " + id);
			// TODO I'd rather throw an exception, but we use this in a few places that throw different exceptions instead
			return currentList;
		}
		return listForTask.get();
	}
	
	private Optional<Task> getTask(long id, String list) {
		TaskList realList = getList(list);
		if (realList.containsTask(id)) {
			return realList.getTask(id);
		}
		return Optional.empty();
	}
	
	public Task renameTask(long id, String task) {
		return getList(findListForTask(id)).renameTask(id, task, writer);
	}

	private void writeTask(Task task, String list) {
		writer.writeTask(task, "git-data/tasks" + list + "/" + task.id + ".txt");
	}

	private void addAndCommit(Task task, String comment, String list) {
		osInterface.runGitCommand("git add tasks" + list + "/" + task.id + ".txt");
		osInterface.runGitCommand("git commit -m \"" + comment + " " + task.description().replace("\"", "\\\"") + "\"");
	}

	private TaskList getList(String name) {
		String absoluteList = getAbsoluteListName(name);

		String groupName = groupNameFromList(absoluteList);


		Optional<TaskGroup> taskGroup = getGroup(groupName);

		if (!taskGroup.isPresent()) {
			throw new RuntimeException("Group '" + groupName + "' not found.");
		}
		return getList(taskGroup.get(), absoluteList);
	}

	public String getAbsoluteListName(String name) {
		if (!name.startsWith("/")) {
			if (activeGroup.getName().equals("/")) {
				return activeGroup.getFullPath() + name;
			}
			return activeGroup.getFullPath() + "/" + name;
		}
		return name;
	}

	private String groupNameFromList(String name) {
		String absoluteList = getAbsoluteListName(name);

		String groupName = absoluteList.substring(0, absoluteList.lastIndexOf('/') + 1);

		if (!groupName.equals("/")) {
			groupName = groupName.substring(0, groupName.length() - 1);
		}

		return groupName;
	}

	private TaskList getList(TaskGroup group, String name) {
		Optional<TaskList> optionalList = group.getListAbsolute(name);

		if (!optionalList.isPresent()) {
			throw new RuntimeException("List '" + name + "' does not exist.");
		}
		return optionalList.get();
	}

	public Set<String> getListNames() {
		return getListNames(rootGroup);
	}

	private Set<String> getListNames(TaskGroup group) {
		Set<String> lists = group.getChildren().stream()
				.filter(child -> child instanceof TaskList)
				.map(TaskContainer::getFullPath)
				.collect(Collectors.toSet());

		group.getChildren().stream()
				.filter(child -> child instanceof TaskGroup)
				.map(child -> (TaskGroup) child)
				.forEach(nestedGroup -> lists.addAll(getListNames(nestedGroup)));

		return lists;
	}
	
	// TODO Can we move most of this to TaskList?
	public Task startTask(long id, boolean finishActive) {
		String taskList = findListForTask(id);

		Optional<Task> first = getList(taskList).getTask(id);
		
		if (first.isPresent()) {
			if (activeTaskID == first.get().id) {
				throw new RuntimeException("Task is already active.");
			}
			if (activeTaskID != NO_ACTIVE_TASK) {
				if (finishActive) {
					finishTask();
				}
				else {
					stopTask();
				}
			}
			activeTaskList = taskList;
			activeTaskID = first.get().id;
			Task activeTask = first.get();
			
			Task newActiveTask = new TaskBuilder(activeTask).activate(osInterface.currentSeconds());
			
			replaceTask(taskList, activeTask, newActiveTask);
			
			setCurrentList(taskList);
			
			writeTask(newActiveTask, currentList);
			addAndCommit(newActiveTask, "Started task", currentList);
			
			return newActiveTask;
		}
		throw new RuntimeException("Task " + id + " was not found.");
	}

	public Task stopTask() {
		Task activeTask = getActiveTask();
		activeTaskID = NO_ACTIVE_TASK;

		Task stoppedTask = new TaskBuilder(activeTask).stop(osInterface.currentSeconds());

		replaceTask(activeTaskList, activeTask, stoppedTask);

		writeTask(stoppedTask, activeTaskList);
		addAndCommit(stoppedTask, "Stopped task", activeTaskList);

		activeTaskList = "";

		return stoppedTask;
	}

	// TODO Move this into the TaskList class
	private void replaceTask(String listName, Task oldTask, Task newTask) {
		TaskList list = getList(listName);
		list.removeTask(oldTask);
		list.addTask(newTask);
	}

	public boolean hasActiveTask() {
		return activeTaskID != -1;
	}

	public boolean setCurrentList(String name) {
		String absoluteList = getAbsoluteListName(name);

		String groupName = groupNameFromList(absoluteList);

		Optional<TaskGroup> group = getGroup(groupName);
		if (group.isPresent()) {
			boolean exists = group.get().containsListAbsolute(absoluteList);
			if (exists) {
				currentList = absoluteList;
			}
			return exists;
		}
		return false;
	}
	
	// TODO I think this should have activeGroup, not rootGroup
	public List<Task> getTasks() {
		return getList(rootGroup, currentList).getTasks();
	}

	public Task finishTask() {
		getActiveTask();
		Task task = getList(getActiveTaskList()).finishTask(activeTaskID, writer);
		
		activeTaskID = NO_ACTIVE_TASK;
		
		return task;
	}
	
	public Task getActiveTask() {
		if (activeTaskID == -1) {
			throw new RuntimeException("No active task.");
		}
		TaskList list = getList(findListForTask(activeTaskID));
		
		Optional<Task> optionalTask = list.getTask(activeTaskID);
		return optionalTask.get();
	}

	public Task finishTask(long id) {
		Optional<Task> optionalTask = getTask(id);

		if (optionalTask.isPresent()) {
			return getList(findListForTask(id)).finishTask(id, writer);
		}
		else {
			throw new RuntimeException("Task not found.");
		}
	}

	public List<Task> getAllTasks() {
		return getAllTasksForGroup(rootGroup);
	}

	private List<Task> getAllTasksForGroup(TaskGroup group) {
		List<Task> tasks = new ArrayList<>();

		group.getChildren().stream()
				.filter(child -> child instanceof TaskList)
				.map(child -> (TaskList) child)
				.forEach(list -> tasks.addAll(list.getTasks()));

		group.getChildren().stream()
				.filter(child -> child instanceof TaskGroup)
				.forEach(child -> tasks.addAll(getAllTasksForGroup((TaskGroup) child)));

		return tasks;
	}

	public List<Task> getTasksForList(String listName) {
		return getList(getAbsoluteListName(listName)).getTasks();
	}

	public Optional<Task> getTask(long id) {
		return getTask(id, currentList);
	}

	public void renameList(String oldName, String newName) {
		String absoluteOldList = getAbsoluteListName(oldName);
		String absoluteNewList = getAbsoluteListName(newName);

		String groupName = groupNameFromList(absoluteOldList);

		Optional<TaskGroup> group = getGroup(groupName);
		if (!group.isPresent()) {
			throw new RuntimeException("Group '" + groupName + "' not found.");
		}
		TaskGroup theGroup = group.get();
		if (!theGroup.containsListAbsolute(absoluteOldList)) {
			throw new RuntimeException("List '" + absoluteOldList + "' not found.");
		}

		if (currentList.equals(absoluteOldList)) {
			currentList = absoluteNewList;
		}

		if (activeTaskList.equals(absoluteOldList)) {
			activeTaskList = absoluteNewList;
		}

		TaskList oldList = getList(theGroup, absoluteOldList);
		theGroup.removeChild(oldList);
		theGroup.addChild(oldList.rename(absoluteNewList));

		for (Task task : getList(theGroup, absoluteNewList).getTasks()) {
			osInterface.removeFile("git-data/tasks" + absoluteOldList + "/" + task.id + ".txt");
			writeTask(task, absoluteNewList);
		}

		osInterface.removeFile("git-data/tasks" + absoluteOldList);

		osInterface.runGitCommand("git add .");
		osInterface.runGitCommand("git commit -m \"Renamed list '" + absoluteOldList + "' to '" + absoluteNewList + "'\"");
	}
	
	public void addTask(Task task) {
		String existingList = findListForTask(task.id);

		if (getTask(task.id, existingList).isPresent()) {
			throw new RuntimeException("Task with ID " + task.id + " already exists.");
		}

		getList(activeGroup, currentList).addTask(task);
		
		if (task.state == TaskState.Active) {
			activeTaskList = currentList;
			activeTaskID = task.id;
		}
	}

	public String getCurrentList() {
		return currentList;
	}

	public String getActiveTaskList() {
		return activeTaskList;
	}
	
	public boolean hasListWithName(String name) {
		String absoluteList = getAbsoluteListName(name);
		String groupName = groupNameFromList(absoluteList);

		return getGroup(groupName).get().containsListAbsolute(absoluteList);
	}

	// TODO Should this stuff go in the TaskGroup class?
	public boolean addList(String name) {
		TaskGroup group;

		String absoluteList = getAbsoluteListName(name);
		String groupName = groupNameFromList(absoluteList);
		
		// create any groups in the path that don't exist
		createGroup(groupName);
		
		group = getGroup(groupName).get();

		if (group.containsListAbsolute(absoluteList)) {
			return false;
		}
		TaskList newList = new TaskList(absoluteList, osInterface);

		group.addChild(newList);

		return true;
	}

	public long getActiveTaskID() {
		return activeTaskID;
	}

	public Task setIssue(long id, long issue) {
		Optional<Task> optionalTask = getTask(id);

		Task task = new TaskBuilder(optionalTask.get())
				.withIssue(issue)
				.build();

		String list = findListForTask(optionalTask.get().id);
		replaceTask(list, optionalTask.get(), task);

		writeTask(task, list);

		osInterface.runGitCommand("git add tasks" + list + "/" + task.id + ".txt");
		osInterface.runGitCommand("git commit -m \"Set issue for task " + task.id + " to " + issue + "\"");

		return task;
	}

	public Task setCharge(long id, String charge) {
		Optional<Task> optionalTask = getTask(id);

		Task task = new TaskBuilder(optionalTask.get())
				.withCharge(charge)
				.build();

		String list = findListForTask(task.id);
		replaceTask(list, optionalTask.get(), task);

		writeTask(task, list);

		osInterface.runGitCommand("git add tasks" + list + "/" + task.id + ".txt");
		osInterface.runGitCommand("git commit -m \"Set charge for task " + task.id + " to '" + charge + "'\"");

		return task;
	}
	
	String getGroupPath() {
		return activeGroup.getFullPath();
	}
	
	public boolean hasGroupPath(String groupName) {
		if (!groupName.startsWith("/")) {
			throw new RuntimeException("Group path must start with root (/).");
		}
		return getGroup(groupName).isPresent();
	}

	private Optional<TaskGroup> getGroup(String name) {
		return groups.stream()
				.filter(group -> group.getFullPath().equals(name))
				.findFirst();
	}

	public TaskGroup createGroup(String groupName) {
		if (!groupName.startsWith("/")) {
			if (activeGroup.getFullPath().equals("/")) {
				groupName = "/" + groupName;
			}
			else {
				groupName = activeGroup.getFullPath() + "/" + groupName;
			}
		}
		String currentParent = "/";
		TaskGroup newGroup = null;
		for (String group : groupName.substring(1).split("/")) {
			if (group.isEmpty()) {
				continue;
			}
			Optional<TaskGroup> parentGroup = getGroup(currentParent);
			newGroup = new TaskGroup(group, currentParent);
			if (currentParent.equals("/")) {
				currentParent += group;
			}
			else {
				currentParent += "/" + group;
			}
			if (!parentGroup.get().containsGroup(newGroup)) {
				try {
					osInterface.createOutputStream("git-data/tasks" + newGroup.getFullPath() + "/group.txt");
				}
				catch (IOException e) {
					e.printStackTrace(output);
				}
				parentGroup.get().addChild(newGroup);
			}
			if (!groups.contains(newGroup)) {
				groups.add(newGroup);
			}
		}

		osInterface.runGitCommand("git add .");
		osInterface.runGitCommand("git commit -m \"Created group '" + groupName + "'\"");

		return newGroup;
	}

	public TaskGroup switchGroup(String groupName) {
		if (!groupName.startsWith("/")) {
			if (activeGroup.getFullPath().equals("/")) {
				groupName = "/" + groupName;
			}
			else {
				groupName = activeGroup.getFullPath() + "/" + groupName;
			}
		}
		if (!getGroup(groupName).isPresent()) {
			throw new RuntimeException("Group '" + groupName + "' does not exist.");
		}
		activeGroup = getGroup(groupName).get();

		return activeGroup;
	}

	public TaskGroup getActiveGroup() {
		return activeGroup;
	}

	public TaskGroup getRootGroup() {
		return rootGroup;
	}
}