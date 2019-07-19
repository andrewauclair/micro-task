// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.Task;
import com.andrewauclair.todo.TaskTimes;
import com.andrewauclair.todo.Tasks;
import com.andrewauclair.todo.jline.ActiveListCompleter;
import com.andrewauclair.todo.jline.ActiveTaskCompleter;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class TimesCommand extends Command {
	private final Tasks tasks;
	
	public TimesCommand(Tasks tasks) {
		this.tasks = tasks;
	}
	
	@Override
	public void execute(PrintStream output, String command) {
		String[] s = command.split(" ");
		
		List<String> parameters = Arrays.asList(s);
		
		// TODO Usage output if format is wrong, can we regex the format or something to verify it?
		if (s.length == 1) {
			output.println("Invalid command.");
			output.println();
			return;
		}
		
		if (s[1].equals("--list")) {
			String list = s[2];
			
			if (parameters.contains("--summary")) {
				output.println("Times summary for list '" + list + "'");
				output.println();
				
				List<Task> listTasks = new ArrayList<>(tasks.getTasksForList(list));
				
				listTasks.sort(Comparator.comparingLong(this::getTotalTaskTime).reversed());
				
				long totalTime = 0;
				
				for (Task task : listTasks) {
					long time = getTotalTaskTime(task);
					
					totalTime += time;
					
					printTotalTime(output, time, true);
					output.println("   " + task.description());
				}
				output.println();
				printTotalTime(output, totalTime, false);
				output.println("     - Total Time");
				output.println();
			}
			else {
				output.println("Times for list '" + list + "'");
				output.println();
				
				long totalTime = 0;
				for (Task task : tasks.getTasksForList(list)) {
					for (TaskTimes time : task.getTimes()) {
						totalTime += getTotalTime(time);
					}
				}
				
				output.print("Total time spent on list: ");
				
				printTotalTime(output, totalTime, false);
				
				output.println();
				output.println();
			}
		}
		else if (s[1].equals("--task") && !parameters.contains("--today")) {
			long taskID = Long.parseLong(s[2]);
			
			String list = tasks.findListForTask(taskID);
			Optional<Task> firstTask = tasks.getTasksForList(list).stream()
					.filter(task -> task.id == taskID)
					.findFirst();
			
			if (firstTask.isPresent()) {
				Task task = firstTask.get();
				
				if (task.getTimes().size() == 0) {
					output.println("No times for task " + task.description());
				}
				else {
					output.println("Times for task " + task.description());
					output.println();
					
					long totalTime = 0;
					for (TaskTimes time : task.getTimes()) {
						output.println(time.description(tasks.osInterface.getZoneId()));
						
						totalTime += getTotalTime(time);
					}
					
					output.println();
					output.print("Total time: ");
					
					printTotalTime(output, totalTime, false);
					output.println();
				}
			}
			else {
				output.println("Task not found.");
			}
			output.println();
		}
		else if (s[1].equals("--tasks") && parameters.contains("--today")) {
			// get date and execute it
			output.print("Times for day ");
			
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
			
			long epochSecond = tasks.osInterface.currentSeconds();
			
			ZoneId zoneId = tasks.osInterface.getZoneId();
			
			output.println(Instant.ofEpochSecond(epochSecond).atZone(zoneId).format(dateTimeFormatter));
			
			LocalTime midnight = LocalTime.MIDNIGHT;
			LocalDate today = LocalDate.ofInstant(Instant.ofEpochSecond(epochSecond), zoneId);//LocalDate.now(zoneId);
			LocalDateTime todayMidnight = LocalDateTime.of(today, midnight);
			LocalDateTime tomorrowMidnight = todayMidnight.plusDays(1);
			
			long midnightStart = todayMidnight.atZone(zoneId).toEpochSecond();
			long midnightStop = tomorrowMidnight.atZone(zoneId).toEpochSecond();
			
			output.println();
			
			long totalTime = 0;
			
			// TODO We should still use getTasksForList if --list was provided, going to skip that for now
			List<Task> listTasks = new ArrayList<>(tasks.getAllTasks());//tasks.getTasksForList(list));
			
			listTasks.sort(Comparator.comparingLong(this::getTotalTaskTime).reversed());
			
			for (Task task : listTasks) {
				boolean include = false;
				long totalTaskTime = 0;
				
				for (TaskTimes time : task.getTimes()) {
					if (time.start >= midnightStart && time.stop < midnightStop) {
						include = true;
						totalTaskTime += getTotalTime(time);
					}
				}
				
				if (include) {
					printTotalTime(output, totalTaskTime, true);
					output.print("   ");
					output.println(task.description());
					
					totalTime += totalTaskTime;
				}
			}
			
			output.println();
			output.print("Total time: ");
			printTotalTime(output, totalTime, false);
			output.println();
			output.println();
		}
		else {
			output.println("Invalid command.");
			output.println();
		}
	}
	
	private long getTotalTaskTime(Task task) {
		long totalTime = 0;
		
		for (TaskTimes time : task.getTimes()) {
			totalTime += getTotalTime(time);
		}
		return totalTime;
	}
	
	private void printTotalTime(PrintStream output, long totalTime, boolean printExtraSpace) {
		long hours = totalTime / (60 * 60);
		long minutes = (totalTime - (hours * 60 * 60)) / 60;
		long seconds = (totalTime - (hours * 60 * 60) - (minutes * 60));
		
		if (hours > 0) {
			output.print(String.format("%02dh ", hours));
		}
		else if (printExtraSpace) {
			output.print("    ");
		}
		
		if (minutes > 0 || hours > 0) {
			output.print(String.format("%02dm ", minutes));
		}
		else if (printExtraSpace) {
			output.print("    ");
		}
		
		output.print(String.format("%02ds", seconds));
	}
	
	private long getTotalTime(TaskTimes time) {
		long totalTime = time.getDuration();
		
		if (time.stop == TaskTimes.TIME_NOT_SET) {
			totalTime += tasks.osInterface.currentSeconds() - time.start;
		}
		return totalTime;
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Arrays.asList(
				node("times",
						node("--list",
								node(new ActiveListCompleter(tasks)),
								node("--today")
						)
				),
				node("times",
						node("--tasks",
								node(new ActiveTaskCompleter(tasks)),
								node("--today")
						)
				)
		);
	}
}
