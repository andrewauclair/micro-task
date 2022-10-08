// Copyright (C) 2022 Andrew Auclair - All Rights Reserved

package com.andrewauclair.v2.microtask.console;

import com.andrewauclair.microtask.LocalSettings;
import com.andrewauclair.microtask.command.Commands;
import com.andrewauclair.microtask.command.UpdateCommand;
import com.andrewauclair.microtask.command.VersionCommand;
import com.andrewauclair.microtask.os.GitLabReleases;
import com.andrewauclair.microtask.os.OSInterfaceImpl;
import com.andrewauclair.microtask.project.Projects;
import com.andrewauclair.microtask.schedule.Schedule;
import com.andrewauclair.microtask.task.DataLoader;
import com.andrewauclair.microtask.task.TaskReader;
import com.andrewauclair.microtask.task.TaskWriter;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.v2.MainFrame;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class MainConsole extends Console {


	private final MainFrame mainFrame;
	private final Commands commands;

	public MainConsole(MainFrame mainFrame, Commands commands) throws Exception {
		super("main", false);
		this.mainFrame = mainFrame;


//		VersionCommand.printLogo(osInterface);
		this.commands = commands;
	}

	public void executeCommand(String command) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		PrintStream print = new PrintStream(stream);
		System.setOut(print);
		System.setErr(print);
		commands.execute(System.out, command);

		String str = stream.toString();

		appendOutput(str);

		mainFrame.executedCommand();
	}

	@Override
	protected void handleInput(String input) {
		executeCommand(input);
	}


}
