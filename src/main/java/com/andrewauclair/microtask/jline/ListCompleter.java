// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.jline;

import com.andrewauclair.microtask.task.Tasks;

import java.util.ArrayList;

public final class ListCompleter extends ArrayList<String> {
	public ListCompleter(Tasks tasks) {
		super(tasks.getInProgressListNames());
	}
}
