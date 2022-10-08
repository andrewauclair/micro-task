// Copyright (C) 2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.v2;

import ModernDocking.Docking;
import ModernDocking.DockingRegion;
import ModernDocking.RootDockingPanel;
import ModernDocking.persist.AppState;
import com.andrewauclair.microtask.LocalSettings;
import com.andrewauclair.microtask.command.Commands;
import com.andrewauclair.microtask.command.UpdateCommand;
import com.andrewauclair.microtask.os.GitLabReleases;
import com.andrewauclair.microtask.os.OSInterfaceImpl;
import com.andrewauclair.microtask.project.Projects;
import com.andrewauclair.microtask.schedule.Schedule;
import com.andrewauclair.microtask.task.DataLoader;
import com.andrewauclair.microtask.task.TaskReader;
import com.andrewauclair.microtask.task.TaskWriter;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.v2.microtask.AddStatusConsoleDialog;
import com.andrewauclair.v2.microtask.console.MainConsole;
import com.andrewauclair.v2.microtask.console.StatusConsole;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainFrame extends JFrame {
	private final OSInterfaceImpl osInterface = new OSInterfaceImpl();

	private final Commands commands;

	private final List<StatusConsole> statusConsoles = new ArrayList<>();
	private final MainConsole main;

	public MainFrame() throws Exception {
		new Docking(this);

		AppState.setAutoPersist(false);
		AppState.setPersistFile(new File("auto_persist_layout.xml"));
		AppState.setAutoPersist(true);

		SwingUtilities.invokeLater(() -> {
			AppState.setAutoPersist(false);
			try {
				AppState.restore();
			}
			catch (Exception e) {}
			AppState.setAutoPersist(true);
		});

		setSize(500, 500);

		LocalSettings localSettings = new LocalSettings(osInterface);

		Tasks tasks = new Tasks(new TaskWriter(osInterface), System.out, osInterface);
		Projects projects = new Projects(tasks, osInterface);
		tasks.setProjects(projects);

		Schedule schedule = new Schedule(tasks, osInterface);
		commands = new Commands(tasks, projects, schedule, new GitLabReleases(osInterface), localSettings, osInterface);

		boolean loadSuccessful = tasks.load(new DataLoader(tasks, new TaskReader(osInterface), localSettings, projects, schedule, osInterface), commands);

		if (requiresTaskUpdate()) {
			UpdateCommand.updateFiles(tasks, osInterface, localSettings, projects, schedule, commands);
		}

		main = new MainConsole(this, commands);

		setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;

//		add(toolBar, gbc);

		gbc.gridy++;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;

		RootDockingPanel dockingPanel = new RootDockingPanel(this);
//		Docking.registerDockingPanel(dockingPanel, this);

		add(dockingPanel, gbc);

		Docking.dock(main, this);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu add = new JMenu("Add");
		JMenuItem newConsole = new JMenuItem("Console...");
		add.add(newConsole);

		menuBar.add(add);

		newConsole.addActionListener(e -> {
			AddStatusConsoleDialog dialog = new AddStatusConsoleDialog(this);
			dialog.setVisible(true);
		});
	}

	// if updateOnInterval is false then we update when the main command changes
	// interval is in seconds
	public void addStatusConsole(String command, boolean updateOnInterval, int interval) {
		StatusConsole statusConsole = new StatusConsole(commands, command, command, updateOnInterval, interval);
		statusConsoles.add(statusConsole);

		Docking.dock(statusConsole, main, DockingRegion.WEST);
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

	public static void main(String[] args) {
		FlatLightLaf.setup();

		SwingUtilities.invokeLater(() -> {
			try {
				new MainFrame().setVisible(true);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	public void executedCommand() {
		// update all the status consoles
		for (final StatusConsole statusConsole : statusConsoles) {
			statusConsole.update();
		}
	}
}
