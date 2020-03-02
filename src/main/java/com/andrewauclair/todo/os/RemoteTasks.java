package com.andrewauclair.todo.os;

import com.andrewauclair.todo.task.Tasks;

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
