// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.os;

import com.andrewauclair.microtask.command.Commands;
import com.andrewauclair.microtask.command.TimesCommand;
import com.andrewauclair.microtask.jline.GroupCompleter;
import com.andrewauclair.microtask.jline.ListCompleter;
import com.andrewauclair.microtask.task.Tasks;
import picocli.CommandLine;

import java.lang.reflect.Constructor;

public final class PicocliFactory implements CommandLine.IFactory {
	private final Commands commands;
	private final Tasks tasks;
	private final OSInterface osInterface;

	public PicocliFactory(Commands commands, Tasks tasks, OSInterface osInterface) {
		this.commands = commands;
		this.tasks = tasks;
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
//		else {
		Constructor<?>[] constructors = cls.getConstructors();
//		return cls.getConstructor(Tasks.class, Commands.class).newInstance(tasks, commands);
//		}
		return CommandLine.defaultFactory().create(cls);
	}
}
