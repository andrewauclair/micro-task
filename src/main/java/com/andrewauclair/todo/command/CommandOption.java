// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class CommandOption {
	static final char NO_SHORTNAME = Character.MIN_VALUE;
	
	private final String name;
	private final char shortName;
	private final String description;
	private final boolean usesName;
	private final List<String> arguments;
	
	public CommandOption(String name, char shortName, boolean usesName) {
		this.name = name;
		this.shortName = shortName;
		this.usesName = usesName;
		arguments = Collections.emptyList();
		this.description = "";
	}

	public CommandOption(String name, char shortName, String description, boolean usesName) {
		this.name = name;
		this.shortName = shortName;
		this.usesName = usesName;
		arguments = Collections.emptyList();
		this.description = description;
	}

	public CommandOption(String name, char shortName, List<String> arguments) {
		this.name = name;
		this.shortName = shortName;
		this.arguments = arguments;
		this.usesName = true;
		this.description = "";
	}

	public CommandOption(String name, char shortName, String description, List<String> arguments) {
		this.name = name;
		this.shortName = shortName;
		this.arguments = arguments;
		this.usesName = true;
		this.description = description;
	}

	public String getName() {
		return name;
	}
	
	char getShortName() {
		return shortName;
	}

	String getDescription() {
		return description;
	}

	boolean usesName() {
		return usesName;
	}
	
	int getArgumentCount() {
		return arguments.size();
	}

	String getArgument(int i) {
		return arguments.get(i);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, arguments, shortName, description, usesName);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CommandOption)) {
			return false;
		}
		CommandOption that = (CommandOption) o;
		return Objects.equals(name, that.name) &&
				Objects.equals(description, that.description) &&
				Objects.equals(arguments, that.arguments) &&
				shortName == that.shortName &&
				usesName == that.usesName;
	}
	
	@Override
	public String toString() {
		return "CommandOption{" +
				"name='" + name + '\'' +
				", shortName=" + shortName +
				", description='" + description + '\'' +
				", arguments=" + arguments +
				", usesName=" + usesName +
				'}';
	}
}
