package com.andrewauclair.todo.git;

public class GitCommand {
	private final String command;
	
	public GitCommand(String command) {
		this.command = command;
	}
	
	@Override
	public String toString() {
		return command;
	}
}
