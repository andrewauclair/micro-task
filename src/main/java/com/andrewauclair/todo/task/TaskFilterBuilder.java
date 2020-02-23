// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

// Provides the ability to create mock TaskFilter in TimesCommand
public class TaskFilterBuilder {
	public TaskTimesFilter createFilter(Tasks tasks) {
		return new TaskTimesFilter(tasks);
	}
}
