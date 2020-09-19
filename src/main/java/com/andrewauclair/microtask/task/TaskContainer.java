// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import java.util.List;
import java.util.Optional;

public interface TaskContainer {
	String getName();

	String getFullPath();

	List<Task> getTasks();

	Optional<TaskList> findListForTask(ExistingID id);

	// TODO Removing these for now because I'm ripping out all the old project/feature stuff
//	String getProject();
//	String getFeature();

	TaskContainerState getState();
}
