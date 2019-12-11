// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import com.andrewauclair.todo.os.OSInterface;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class TaskLoader {
	private final Tasks tasks;
	private final TaskReader reader;
	private final OSInterface osInterface;
	
	public TaskLoader(Tasks tasks, TaskReader reader, OSInterface osInterface) {
		this.tasks = tasks;
		this.reader = reader;
		this.osInterface = osInterface;
	}
	
	
	public void load() throws IOException {
		loadTasks("git-data/tasks");
	}
	
	private void loadTasks(String folder) throws IOException {
		for (OSInterface.TaskFileInfo fileInfo : osInterface.listFiles(folder)) {
			if (fileInfo.isDirectory()) {
				String name = fileInfo.getFileName();
				
				boolean isGroup = isGroupFolder(fileInfo.getPath());
				if (isGroup) {
					tasks.createGroup(name + "/");
					tasks.switchGroup(name + "/");
					
					try (InputStream inputStream = osInterface.createInputStream(folder + "/" + name + "/group.txt")) {
						Scanner scanner = new Scanner(inputStream);
						
						if (scanner.hasNextLine()) {
							tasks.setProject(tasks.getGroup(name + "/"), scanner.nextLine());
							tasks.setFeature(tasks.getGroup(name + "/"), scanner.nextLine());
						}
					}
					catch (IOException ignored) {
					}
				}
				else {
					tasks.addList(name);
					tasks.setActiveList(name);
					
					try (InputStream inputStream = osInterface.createInputStream(folder + "/" + name + "/list.txt")) {
						Scanner scanner = new Scanner(inputStream);
						
						tasks.setProject(tasks.getListByName(name), scanner.nextLine());
						tasks.setFeature(tasks.getListByName(name), scanner.nextLine());
					}
					catch (IOException ignored) {
					}
				}
				loadTasks(fileInfo.getPath());
				
				if (isGroup) {
					tasks.switchGroup(tasks.getActiveGroup().getParent());
				}
			}
			else if (!fileInfo.getFileName().equals("group.txt") &&
					!fileInfo.getFileName().equals("list.txt")) {
				String fileName = fileInfo.getFileName();
				long id = Long.parseLong(fileName.substring(fileName.lastIndexOf('/') + 1, fileName.indexOf(".txt")));
				
				Task task = reader.readTask(id, fileInfo.getPath());
				tasks.addTask(task);
			}
		}
	}
	
	private boolean isGroupFolder(String folder) {
		return osInterface.listFiles(folder).stream()
				.anyMatch(fileInfo -> fileInfo.getFileName().equals("group.txt"));
	}
}
