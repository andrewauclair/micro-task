// Copyright (C) 2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.os.OSInterface;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public class TaskIDValidator implements IDValidator {
	// TODO temporarily public to ease refactoring
	private long nextID = 1;
	private long nextShortID = 1;

	public final Set<Long> existingTasksFullIDs = new HashSet<>();

	private final Map<RelativeTaskID, FullTaskID> shortIDToFullID = new HashMap<>();

	private final PrintStream output;
	private final OSInterface osInterface;

	public TaskIDValidator(PrintStream output, OSInterface osInterface) {
		this.output = output;
		this.osInterface = osInterface;
	}

	@Override
	public long nextID() {
		return nextID;
	}

	// used to set the next ID when loading from files
	@Override
	public void setStartingID(long id) {
		nextID = id;
	}

	// increment the ID and return the result
	// TODO this should be changed to NewID or ExistingID
	@Override
	public NewID incrementID() {
		long nextID = this.nextID++;

		writeNextID();

		return new NewID(this, nextID);
	}

	private void writeNextID() {
		try (PrintStream outputStream = new PrintStream(osInterface.createOutputStream("git-data/next-id.txt"))) {
			outputStream.print(this.nextID);
		}
		catch (IOException e) {
			e.printStackTrace(output);
		}
	}

	@Override
	public long nextShortID() {
		return nextShortID;
	}

	@Override
	public long incrementShortID() {
		return nextShortID++;
	}

	@Override
	public void resetShortIDs() {
		nextShortID = 1;
		shortIDToFullID.clear();
	}

	@Override
	public boolean containsShortID(RelativeTaskID id) {
		return id.ID() < nextShortID;
	}

	@Override
	public void mapShortIDToFullID(RelativeTaskID shortID, FullTaskID fullID) {
		shortIDToFullID.put(shortID, fullID);
	}

	@Override
	public FullTaskID fullIDFromShortID(RelativeTaskID shortID) {
		if (shortIDToFullID.containsKey(shortID)) {
			return shortIDToFullID.get(shortID);
		}
		throw new TaskException("Task with relative ID " + shortID.ID() + " does not exist.");
	}

	@Override
	public void addExistingID(long id) {
		existingTasksFullIDs.add(id);
	}

	@Override
	public boolean containsExistingID(long id) {
		return existingTasksFullIDs.contains(id);
	}

	@Override
	public void clearExistingIDs() {
		existingTasksFullIDs.clear();
	}
}
