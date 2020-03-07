package com.andrewauclair.microtask.os;

import com.andrewauclair.microtask.task.Tasks;

import java.rmi.Remote;

public class RemoteTasks implements Remote {
	private final Tasks tasks;

	public RemoteTasks(Tasks tasks) {
		this.tasks = tasks;
	}

	public Tasks getTasks() {
		return tasks;
	}
}
