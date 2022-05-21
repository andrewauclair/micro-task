// Copyright (C) 2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

public interface IDValidator {
	long nextID();
	void setStartingID(long id);
	long incrementID();
	long nextShortID();
	long incrementShortID();
	void resetShortIDs();

	boolean containsShortID(RelativeTaskID id);

	void mapShortIDToFullID(RelativeTaskID shortID, FullTaskID fullID);

	FullTaskID fullIDFromShortID(RelativeTaskID shortID);

	// TODO refactor these, have to add them for now
	void addExistingID(long id);
	boolean containsExistingID(long id);
	void clearExistingIDs();
}
