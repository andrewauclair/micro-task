// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.TaskException;
import com.andrewauclair.todo.Utils;
import com.andrewauclair.todo.jline.GroupCompleter;
import com.andrewauclair.todo.jline.ListCompleter;
import com.andrewauclair.todo.os.ConsoleColors;
import com.andrewauclair.todo.os.OSInterface;
import com.andrewauclair.todo.task.*;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.andrewauclair.todo.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.todo.os.ConsoleColors.ANSI_REVERSED;

@CommandLine.Command(name = "times")
public class TimesCommand extends Command {
	@Option(names = {"--info"})
	private boolean info;
	
	@Option(names = {"--proj-feat"})
	private boolean proj_feat;
	
	@Option(names = {"--list"}, completionCandidates = ListCompleter.class)
	private String[] list;
	
	@Option(names = {"--group"}, completionCandidates = GroupCompleter.class)
	private String[] group;
	
	@Option(names = {"--today"})
	private boolean today;
	
	@Option(names = {"--yesterday"})
	private boolean yesterday;
	
	@Option(names = {"-d", "--day"})
	private Integer day;
	
	@Option(names = {"-m", "--month"})
	private Integer month;
	
	@Option(names = {"-y", "--year"})
	private Integer year;
	
	@Option(names = {"--week"})
	private boolean week;
	
	@Option(names = {"--all-time"})
	private boolean all_time;
	
	private final Tasks tasks;
	private final OSInterface osInterface;
	
	TimesCommand(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}
	
	private void displayTimesForDay(Instant day, TaskTimesFilter filter) {
		// get date and execute it
		System.out.print("Times for day ");
		
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
		
		ZoneId zoneId = osInterface.getZoneId();
		
		System.out.println(day.atZone(zoneId).format(dateTimeFormatter));
		
		boolean printListNames = list != null;
		
		if (!printListNames) {
			System.out.println();
		}
		
		displayTimes(filter, printListNames);
	}
	
	private void displayTimes(TaskTimesFilter filter, boolean individualLists) {
		List<TaskTimesFilter.TaskTimeFilterResult> results = filter.getData();
		
		long maxID = Long.MIN_VALUE;
		long minID = Long.MAX_VALUE;
		
		for (TaskTimesFilter.TaskTimeFilterResult result : results) {
			if (result.task.id > maxID) {
				maxID = result.task.id;
			}
			if (result.task.id < minID) {
				minID = result.task.id;
			}
		}
		
		long idSpace = Long.toString(maxID).length() - Long.toString(minID).length() + 1;
		
		if (individualLists) {
			Map<String, List<TaskTimesFilter.TaskTimeFilterResult>> map = new HashMap<>();
			
			for (TaskTimesFilter.TaskTimeFilterResult datum : results) {
				List<TaskTimesFilter.TaskTimeFilterResult> result = map.getOrDefault(datum.list, new ArrayList<>());
				result.add(datum);
				map.put(datum.list, result);
			}
			
			List<List<TaskTimesFilter.TaskTimeFilterResult>> lists = new ArrayList<>();
			
			for (String s : map.keySet()) {
				lists.add(map.get(s));
			}
			
			lists.sort(
					(o1, o2) -> {
						long total_1 = 0;
						long total_2 = 0;
						
						for (TaskTimesFilter.TaskTimeFilterResult taskTimeFilterResult : o1) {
							total_1 += taskTimeFilterResult.getTotal();
						}
						
						for (TaskTimesFilter.TaskTimeFilterResult taskTimeFilterResult : o2) {
							total_2 += taskTimeFilterResult.getTotal();
						}
						
						return Long.compare(total_2, total_1);
					}
			);
			
			long totalTime = 0;
			
			for (List<TaskTimesFilter.TaskTimeFilterResult> taskTimeFilterResults : lists) {
				taskTimeFilterResults.sort(Comparator.comparingLong(TaskTimesFilter.TaskTimeFilterResult::getTotal).reversed());
				
				System.out.println();
				System.out.print(ConsoleColors.ANSI_BOLD);
				System.out.print(taskTimeFilterResults.get(0).list);
				System.out.print(ANSI_RESET);
				System.out.println();
				totalTime += printResults(taskTimeFilterResults, getHighestTime(filter), idSpace);
			}
			
			System.out.println();
			System.out.print(Utils.formatTime(totalTime, getHighestTime(filter)));
		}
		else {
			results.sort(Comparator.comparingLong(TaskTimesFilter.TaskTimeFilterResult::getTotal).reversed());
			
			long totalTime = printResults(results, getHighestTime(filter), idSpace);
			
			System.out.println();
			System.out.print(Utils.formatTime(totalTime, getHighestTime(filter)));
		}
		System.out.print("   Total");
		System.out.println();
		System.out.println();
	}
	
	private Utils.HighestTime getHighestTime(TaskTimesFilter filter) {
		Utils.HighestTime highestTime = Utils.HighestTime.Second;
		
		long totalTime = 0;
		
		for (TaskTimesFilter.TaskTimeFilterResult result : filter.getData()) {
			Utils.HighestTime resultHighest = Utils.fromTimestamp(result.total);
			
			if (resultHighest.isAtLeast(highestTime)) {
				highestTime = resultHighest;
			}
			
			totalTime += result.total;
		}
		
		Utils.HighestTime totalHighest = Utils.fromTimestamp(totalTime);
		
		if (totalHighest.isAtLeast(highestTime)) {
			highestTime = totalHighest;
		}
		
		return highestTime;
	}
	
	private long printResults(List<TaskTimesFilter.TaskTimeFilterResult> data, Utils.HighestTime highestTime, long idSpace) {
		long totalTime = 0;
		
		for (TaskTimesFilter.TaskTimeFilterResult result : data) {
			System.out.print(Utils.formatTime(result.getTotal(), highestTime));
			
			Task task = result.getTask();
			
			String idPad = String.join("", Collections.nCopies((int) (idSpace - Long.toString(task.id).length()), " "));
			
			if (tasks.getActiveTaskID() == task.id) {
				System.out.print(" * ");
				System.out.print(idPad);
				ConsoleColors.println(System.out, ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN, task.description());
			}
			else if (task.state == TaskState.Finished) {
				System.out.print(" F ");
				System.out.print(idPad);
				System.out.println(task.description());
			}
			else if (task.isRecurring()) {
				System.out.print(" R ");
				System.out.print(idPad);
				System.out.println(task.description());
			}
			else {
				System.out.print("   ");
				System.out.print(idPad);
				System.out.println(task.description());
			}
			
			totalTime += result.getTotal();
		}
		return totalTime;
	}
	
	private void printTasks(TaskTimesFilter filter) {
		displayTimes(filter, false);
	}
	
	@Override
	public void run() {
		TaskTimesFilter filter = tasks.getFilterBuilder().createFilter(tasks);
		
		if (list != null) {
			Arrays.stream(list).forEach(list -> filter.filterForList(tasks.getAbsoluteListName(list)));
		}
		
		if (group != null) {
			Arrays.stream(group).forEach(group -> filter.filterForGroup(tasks.getGroup(group)));
		}
		
		Instant instant = Instant.ofEpochSecond(osInterface.currentSeconds());
		
		if (week) {
			LocalDate day = LocalDate.ofInstant(instant, osInterface.getZoneId());
			
			instant = day.minusDays(day.getDayOfWeek().getValue()).atStartOfDay(osInterface.getZoneId()).toInstant();
			
			filter.filterForWeek(day.getMonth().getValue(), day.getDayOfMonth(), day.getYear());
		}
		else if (today) {
			LocalDate day = LocalDate.ofInstant(instant, osInterface.getZoneId());
			
			filter.filterForDay(day.getMonth().getValue(), day.getDayOfMonth(), day.getYear());
		}
		else if (yesterday) {
			long epochSecond = osInterface.currentSeconds() - (60 * 60 * 24);
			
			instant = Instant.ofEpochSecond(epochSecond);
			
			LocalDate day = LocalDate.ofInstant(instant, osInterface.getZoneId());
			
			filter.filterForDay(day.getMonth().getValue(), day.getDayOfMonth(), day.getYear());
		}
		else if (day != null) {
			ZoneId zoneId = osInterface.getZoneId();
			
			if (day < 1 || day > 31) {
				throw new TaskException("Day option must be 1 - 31");
			}
			
			if (month != null && (month < 1 || month > 12)) {
				throw new TaskException("Month option must be 1 - 12");
			}
			
			int day = this.day;
			int month = this.month != null ? this.month : instant.atZone(zoneId).getMonthValue();
			int year = this.year != null ? this.year : instant.atZone(zoneId).getYear();
			
			LocalDate of = LocalDate.of(year, month, day);
			
			instant = of.atStartOfDay(zoneId).toInstant();
			
			filter.filterForDay(month, day, year);
		}
		
		if ((list != null || group != null) && day == null && !today) {
			List<String> lists = new ArrayList<>();
			
			if (list != null) {
				lists.addAll(Arrays.asList(list));
			}
			
			if (group != null) {
				for (String group : group) {
					lists.addAll(tasks.getGroup(group).getChildren().stream()
							.filter(child -> child instanceof TaskList)
							.map(TaskContainer::getFullPath)
							.collect(Collectors.toSet()));
				}
			}
			
			if (list != null && lists.size() > 1) {
				System.out.println("Times for multiple lists");
			}
			else if (group != null && group.length > 1) {
				System.out.println("Times for multiple groups");
			}
			else if (group != null) {
				System.out.println("Times for group '" + tasks.getAbsoluteGroupName(group[0]) + "'");
			}
			else {
				String list = tasks.getAbsoluteListName(lists.get(0));
				
				System.out.println("Times for list '" + list + "'");
				System.out.println();
			}
			
			if (group != null && list == null) {
				displayTimes(filter, true);
			}
			else {
				displayTimes(filter, lists.size() > 1);
			}
		}
		else if (info) {
			displayLog(filter);
		}
		else if (today && !proj_feat) {
			displayTimesForDay(Instant.ofEpochSecond(osInterface.currentSeconds()), filter);
		}
		else if (yesterday && !proj_feat) {
			displayTimesForDay(instant, filter);
		}
		else if (week && !proj_feat) {
			System.out.print("Times for week of ");
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
			
			ZoneId zoneId = osInterface.getZoneId();
			
			System.out.println(instant.atZone(zoneId).format(dateTimeFormatter));
			
			System.out.println();
			displayTimes(filter, false);
		}
		else if (proj_feat) {
			if (all_time || today || yesterday || week || day != null) {
				displayProjectsFeatures(filter);
			}
			else {
				System.out.println("Invalid command.");
				System.out.println();
			}
		}
		else {
			if (all_time) {
				System.out.println("Times");
				System.out.println();
				printTasks(new TaskTimesFilter(tasks));
			}
			else if (day != null) {
				displayTimesForDay(instant, filter);
			}
			else {
				System.out.println("Invalid command.");
				System.out.println();
			}
		}
	}
	
	private static class InfoData {
		final long time;
		final TaskState state;
		
		private InfoData(long time, TaskState state) {
			this.time = time;
			this.state = state;
		}
		
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			InfoData infoData = (InfoData) o;
			return time == infoData.time &&
					state == infoData.state;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(time, state);
		}
	}
	
	private void displayLog(TaskTimesFilter filter) {
		Map<InfoData, Task> data = new HashMap<>();
		
		for (TaskTimesFilter.TaskTimeFilterResult result : filter.getData()) {
		
		}
		
		new ArrayList<>(data.keySet()).sort(new Comparator<InfoData>() {
			@Override
			public int compare(InfoData o1, InfoData o2) {
				return Long.compare(o1.time, o2.time);
			}
		});
	}
	
	private void displayProjectsFeatures(TaskTimesFilter filter) {
		Map<String, Long> totals = new HashMap<>();
		long totalTime = 0;
		
		for (TaskTimesFilter.TaskTimeFilterResult task : filter.getData()) {
			String project = tasks.getProjectForTask(task.task.id);
			String feature = tasks.getFeatureForTask(task.task.id);
			
			if (project.isEmpty()) {
				project = "None";
			}
			if (feature.isEmpty()) {
				feature = "None";
			}
			String projfeat = project + " / " + feature;
			
			totals.put(projfeat, totals.getOrDefault(projfeat, 0L) + task.total);
			totalTime += task.total;
		}
		
		List<String> str = new ArrayList<>(totals.keySet());
		str.sort(String::compareTo);
		
		Optional<String> longest = str.stream()
				.max(Comparator.comparingInt(String::length));
		
		for (String s1 : str) {
			String projFeat = s1;
			if (projFeat.contains("None")) {
				projFeat = projFeat.replaceAll("None", ANSI_REVERSED + "None" + ANSI_RESET);
			}
			System.out.print(Utils.formatTime(totals.get(s1), getHighestTime(filter)));
			System.out.print(String.join("", Collections.nCopies(longest.get().length() - s1.length() + 3, " ")));
			System.out.print(projFeat);
			
			System.out.println();
		}
		System.out.println();
		System.out.print(Utils.formatTime(totalTime, getHighestTime(filter)));
		System.out.print("   Total");
		System.out.println();
		System.out.println();
	}
}
