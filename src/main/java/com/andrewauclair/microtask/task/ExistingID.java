// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;

import java.util.Objects;

public class ExistingID {
	private final long id;

	public ExistingID(Tasks tasks, long id) {
		if (id > 0) {
			this.id = id;

			if (!tasks.idValidator().containsExistingID(id)) {
				throw new TaskException("Task " + id + " does not exist.");
			}
		}
		else {
			RelativeTaskID shortID = new RelativeTaskID(id * -1);

			if (tasks.idValidator().containsShortID(shortID)) {
				this.id = tasks.idValidator().fullIDFromShortID(shortID).ID();
			}
			else {
				throw new TaskException("Task with relative ID " + shortID + " does not exist.");
			}
		}
	}

	public long get() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ExistingID that = (ExistingID) o;
		return id == that.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "ExistingID{" +
				"id=" + id +
				'}';
	}
}
