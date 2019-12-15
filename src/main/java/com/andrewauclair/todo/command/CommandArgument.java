// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import java.util.Objects;

public final class CommandArgument {
	private final String name;
	private final String value;
	
	CommandArgument(String name) {
		this.name = name;
		this.value = "";
	}
	
	CommandArgument(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	String getValue() {
		return value;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(name, value);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CommandArgument)) {
			return false;
		}
		CommandArgument that = (CommandArgument) o;
		return Objects.equals(name, that.name) &&
				Objects.equals(value, that.value);
	}
	
	@Override
	public String toString() {
		return "CommandArgument{" +
				"name='" + name + '\'' +
				", value='" + value + '\'' +
				'}';
	}
}
