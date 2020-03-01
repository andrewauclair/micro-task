package com.andrewauclair.todo.jline;

import com.andrewauclair.todo.task.Tasks;

import java.util.ArrayList;

public final class GroupCompleter extends ArrayList<String> {
	public GroupCompleter(Tasks tasks) {
		super(tasks.getGroupNames());
	}
}
