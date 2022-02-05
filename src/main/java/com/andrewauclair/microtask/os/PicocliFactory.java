// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.os;

import com.andrewauclair.microtask.command.Commands;
import com.andrewauclair.microtask.jline.GroupCompleter;
import com.andrewauclair.microtask.jline.ListCompleter;
import com.andrewauclair.microtask.jline.ProjectCompleter;
import com.andrewauclair.microtask.project.Projects;
import com.andrewauclair.microtask.task.Tasks;
import com.sun.jna.platform.mac.SystemB;
import picocli.CommandLine;

import java.lang.reflect.Constructor;

public final class PicocliFactory implements CommandLine.IFactory {
	private final Commands commands;
	private final Tasks tasks;
	private final Projects projects;
	private final OSInterface osInterface;

	public PicocliFactory(Commands commands, Tasks tasks, Projects projects, OSInterface osInterface) {
		this.commands = commands;
		this.tasks = tasks;
		this.projects = projects;
		this.osInterface = osInterface;
	}

	@Override
	public <K> K create(Class<K> cls) throws Exception {
		if (cls == ListCompleter.class) {
			return cls.cast(new ListCompleter(tasks));
		}
		else if (cls == GroupCompleter.class) {
			return cls.cast(new GroupCompleter(tasks));
		}
		else if (cls == ProjectCompleter.class) {
			return cls.cast(new ProjectCompleter(projects));
		}
		else if (cls == MainConsole.MicroTaskHelpCommand.class) {
			return cls.cast(new MainConsole.MicroTaskHelpCommand(commands, osInterface));
		}
		return CommandLine.defaultFactory().create(cls);
	}
}
