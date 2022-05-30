// Copyright (C) 2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

public class TaskID {
	private final long longID;
	private final long shortID;

	public TaskID(long longID) {
		this.longID = longID;
		this.shortID = -1;
	}

	public TaskID(long longID, long shortID) {
		this.longID = longID;
		this.shortID = shortID;
	}

	boolean hasShortID() {
		return shortID != -1;
	}


}
