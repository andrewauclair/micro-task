// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.jline;

import com.andrewauclair.microtask.task.Tasks;

import java.util.ArrayList;

public final class GroupCompleter extends ArrayList<String> {
	public GroupCompleter(Tasks tasks) {
		super(tasks.getInProgressGroupNames());
	}
}
