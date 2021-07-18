// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.os;

import com.andrewauclair.microtask.LocalSettings;
import com.andrewauclair.microtask.command.Commands;
import com.andrewauclair.microtask.command.UpdateCommand;
import com.andrewauclair.microtask.command.VersionCommand;
import com.andrewauclair.microtask.os.StatusConsole.TransferType;
import com.andrewauclair.microtask.project.Projects;
import com.andrewauclair.microtask.schedule.Schedule;
import com.andrewauclair.microtask.task.*;
import com.sun.jna.platform.win32.Kernel32;
import org.jline.reader.*;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.InfoCmp;
import org.jline.utils.OSUtils;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import static java.util.Comparator.comparingLong;

public class MainConsole extends CommonConsole {
	private static final String CONSOLE_TITLE = "micro task main";

	private final OSInterfaceImpl osInterface = new OSInterfaceImpl();

	private final Commands commands;
	private final Tasks tasks;
	private final Projects projects;

	public MainConsole() throws Exception {
		Thread listen = new Thread(() -> {
			try {
				osInterface.openStatusLink();
			}
			catch (IOException e) {
				e.printStackTrace(System.out);
			}
		});
		listen.start();

		LocalSettings localSettings = new LocalSettings(osInterface);

		tasks = new Tasks(new TaskWriter(osInterface), System.out, osInterface);
		projects = new Projects(tasks, osInterface);
		tasks.setProjects(projects);

		commands = new Commands(tasks, projects, new Schedule(tasks, osInterface), new GitLabReleases(), localSettings, osInterface);

		boolean loadSuccessful = tasks.load(new DataLoader(tasks, new TaskReader(osInterface), localSettings, projects, osInterface), commands);

		if (requiresTaskUpdate()) {
			UpdateCommand.updateFiles(tasks, osInterface, localSettings, projects, commands);
		}

		sendCurrentStatus();

		try (Terminal terminal = osInterface.terminal()) {
			LineReader lineReader = configureTerminal(commands, terminal);

			if (loadSuccessful) {
//				lineReader.getBuiltinWidgets().get(LineReader.CLEAR_SCREEN).apply();
			}

			VersionCommand.printLogo(osInterface);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		final Kernel32 kernel32 = Kernel32.INSTANCE;

		kernel32.SetConsoleTitle(CONSOLE_TITLE);

		run();
	}

	private void run() {
		while (true) {
			String command;

			try (Terminal terminal = osInterface.terminal()) {
				LineReader lineReader = configureTerminal(commands, terminal);

				osInterface.terminal = terminal;

				command = lineReader.readLine(commands.getPrompt());

				if (command.equals("proj-feat-assign")) {
//					manualProjectFeatureAssign(tasks);
				}

				if (command.equals("export")) {
//					exportData(tasks);
				}
				else if (command.equals("size")) {
					System.out.println(terminal.getSize());
				}
				else {
					commands.execute(System.out, command);
				}
			}
			catch (UserInterruptException ignored) {
			}
			catch (EndOfFileException e) {
				return;
			}
			catch (IOException e) {
				e.printStackTrace();
			}

			sendCurrentStatus();
		}
	}

	private void sendCurrentStatus() {
		osInterface.sendStatusMessage(TransferType.CURRENT_GROUP, tasks.getCurrentGroup().getFullPath());
		osInterface.sendStatusMessage(TransferType.CURRENT_LIST, tasks.getCurrentList().absoluteName());
		osInterface.sendStatusMessage(TransferType.END_TRANSFER);
	}

	// TODO Find a way to test this, build it into the task loader as the last step
	private boolean requiresTaskUpdate() {
		String currentVersion = "";

		try {
			currentVersion = osInterface.getVersion();
		}
		catch (IOException ignored) {
		}

		try (InputStream inputStream = osInterface.createInputStream("git-data/task-data-version.txt")) {
			Scanner scanner = new Scanner(inputStream);

			String dataVersion = scanner.nextLine();

			return !currentVersion.equals(dataVersion);
		}
		catch (Exception ignored) {
		}
		// if the file doesn't exist, then yes, we need to update
		return true;
	}

	/**
	 //	 * Top-level command that just prints help.
	 //	 */
	@Command(name = "",
			description = {
					"",
					"micro task, a CLI based task tracking application."},
			synopsisSubcommandLabel = "COMMAND",
			subcommands = {MicroTaskHelpCommand.class}
	)
	public static final class CliCommands implements Runnable {
		public CliCommands() {
		}

		public void run() {
			System.out.println("Help output!");
			System.out.println(new CommandLine(this).getUsageMessage());
		}
	}

	@Command(name = "help", helpCommand = true)
	public static final class MicroTaskHelpCommand implements Runnable {
		private final Commands commands;
		private final OSInterface osInterface;

		public MicroTaskHelpCommand(Commands commands, OSInterface osInterface) {
			this.commands = commands;
			this.osInterface = osInterface;
		}

		@Override
		public void run() {
			VersionCommand.printLogo(osInterface);
			System.out.println();

			commands.buildCommandLineWithAllCommands().usage(System.out);

			System.out.println();
			System.out.println("Aliases:");
			System.out.println();

			Map<String, String> aliases = commands.getAliases();

			Optional<String> max = aliases.keySet().stream()
					.max(comparingLong(String::length));

			for (final String alias : aliases.keySet()) {
				System.out.print("  ");
				System.out.println(String.format("%-" + max.get().length() + "s  %s", alias, aliases.get(alias)));
			}
			System.out.println();
		}
	}
}
