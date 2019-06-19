// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.git;

import java.util.Objects;

public final class GitCommand {
	private final String command;
	
	public GitCommand(String command) {
		this.command = command;
	}
	
	@Override
	public String toString() {
		return command;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		GitCommand that = (GitCommand) o;
		return Objects.equals(command, that.command);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(command);
	}
}
