// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import java.util.List;
import java.util.Optional;

public interface TaskContainer {
	String getName();

	String getFullPath();

	List<Task> getTasks();

	Optional<TaskList> findListForTask(long id);

	String getProject();

	String getFeature();

	TaskContainerState getState();
}