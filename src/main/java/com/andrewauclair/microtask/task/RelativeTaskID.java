// Copyright (C) 2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import java.util.Objects;

public final class RelativeTaskID {
	public static final RelativeTaskID NO_SHORT_ID = new RelativeTaskID(-1);

	private final long id;

	public RelativeTaskID(long id) {
		this.id = id;
	}

	public long ID() {
		return id;
	}

	public boolean isValid() {
		return this != NO_SHORT_ID;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final RelativeTaskID that = (RelativeTaskID) o;
		return id == that.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return Long.toString(id);
	}
}
