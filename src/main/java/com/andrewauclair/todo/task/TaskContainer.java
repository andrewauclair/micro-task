// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import java.util.List;
import java.util.Optional;

public interface TaskContainer {
	String getName();

	String getFullPath();

	List<Task> getTasks();
	
	Optional<TaskList> findListForTask(long id);
}
