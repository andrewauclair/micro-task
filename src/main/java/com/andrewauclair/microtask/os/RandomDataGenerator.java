// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.os;

import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.group.name.NewTaskGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import com.andrewauclair.microtask.task.list.name.NewTaskListName;
import picocli.CommandLine;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

@CommandLine.Command(name = "datagen", hidden = true)
public class RandomDataGenerator implements Runnable {
	private final Tasks tasks;

	@CommandLine.Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@CommandLine.Option(names = {"-i", "--iterations"})
	private int iterations = 2000;

	@CommandLine.Option(names = {"-g", "--group-percent"})
	private int groupPercent = 20;

	@CommandLine.Option(names = {"-l", "--list-percent"})
	private int listPercent = 30;

	@CommandLine.Option(names = {"-t", "--task-percent"})
	private int taskPercent = 50;

	Random random = new Random(System.nanoTime());

	//	@CommandLine.Option(names = {"-"})
	public RandomDataGenerator(Tasks tasks) {
		this.tasks = tasks;
	}

	List<String> animals = new ArrayList<>();

	@Override
	public void run() {
		Scanner scanner;
		try {
			scanner = new Scanner(new File("animals.txt"));
			while (scanner.hasNext()) {
				animals.add(scanner.next());
			}

			for (int i = 0; i < iterations; i++) {
				generateData();
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}


	}

	private void generateData() {
		if (randVal(groupPercent)) {
			NewTaskGroupName groupName = new NewTaskGroupName(tasks, animals.get(0) + "/");
			animals.remove(0);
			tasks.createGroup(groupName);
//			tasks.setActiveGroup(new ExistingTaskGroupName(tasks, groupName.absoluteName()));

			System.out.println("Created group '" + groupName + "'");
		}
		else if (randVal(listPercent)) {
			NewTaskListName listName = new NewTaskListName(tasks, animals.get(0));
			animals.remove(0);
			tasks.addList(listName, true);
			tasks.setCurrentList(new ExistingListName(tasks, listName.absoluteName()));

			System.out.println("Created list '" + listName + "'");
		}
		else if (randVal(taskPercent)) {
			Task task = tasks.addTask(animals.get(0));
			animals.remove(0);

			System.out.println("Created task " + task.description());
		}
	}

	private boolean randVal(int percent) {
		return random.nextInt(100) < percent;
	}
}
