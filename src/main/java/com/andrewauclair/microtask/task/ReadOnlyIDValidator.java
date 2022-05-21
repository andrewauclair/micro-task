// Copyright (C) 2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

public class ReadOnlyIDValidator implements IDValidator {
	@Override
	public long nextID() {
		throw new RuntimeException("ReadOnlyIDValidator should never be called");
	}

	@Override
	public void setStartingID(long id) {
		throw new RuntimeException("ReadOnlyIDValidator should never be called");
	}

	@Override
	public long incrementID() {
		throw new RuntimeException("ReadOnlyIDValidator should never be called");
	}

	@Override
	public long nextShortID() {
		throw new RuntimeException("ReadOnlyIDValidator should never be called");
	}

	@Override
	public long incrementShortID() {
		throw new RuntimeException("ReadOnlyIDValidator should never be called");
	}

	@Override
	public void resetShortIDs() {
		throw new RuntimeException("ReadOnlyIDValidator should never be called");
	}

	@Override
	public boolean containsShortID(RelativeTaskID id) {
		throw new RuntimeException("ReadOnlyIDValidator should never be called");
	}

	@Override
	public void mapShortIDToFullID(RelativeTaskID shortID, FullTaskID fullID) {
		throw new RuntimeException("ReadOnlyIDValidator should never be called");
	}

	@Override
	public FullTaskID fullIDFromShortID(RelativeTaskID shortID) {
		throw new RuntimeException("ReadOnlyIDValidator should never be called");
	}

	@Override
	public void addExistingID(long id) {
		throw new RuntimeException("ReadOnlyIDValidator should never be called");
	}

	@Override
	public boolean containsExistingID(long id) {
		throw new RuntimeException("ReadOnlyIDValidator should never be called");
	}

	@Override
	public void clearExistingIDs() {
		throw new RuntimeException("ReadOnlyIDValidator should never be called");
	}
}
