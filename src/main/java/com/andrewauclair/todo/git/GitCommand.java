package com.andrewauclair.todo.git;

class GitCommand {
	private final String command;
	
	GitCommand(String command) {
		this.command = command;
	}
	
	@Override
	public String toString() {
		return command;
	}
}
