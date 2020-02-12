// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.os.OSInterface;
import org.jline.builtins.Completers;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

@CommandLine.Command(name = "version")
public class VersionCommand extends Command {
	private final OSInterface osInterface;
	
	public VersionCommand(OSInterface osInterface) {
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		try {
			System.out.println(osInterface.getVersion());
		}
		catch (IOException e) {
			System.out.println("Unknown");
		}
		System.out.println();
	}
}
