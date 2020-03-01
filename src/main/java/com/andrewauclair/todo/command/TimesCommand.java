// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.TaskException;
import com.andrewauclair.todo.Utils;
import com.andrewauclair.todo.jline.GroupCompleter;
import com.andrewauclair.todo.jline.ListCompleter;
import com.andrewauclair.todo.os.ConsoleColors;
import com.andrewauclair.todo.os.OSInterface;
import com.andrewauclair.todo.task.*;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.lang.reflect.Array;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.andrewauclair.todo.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.todo.os.ConsoleColors.ANSI_REVERSED;
import static com.andrewauclair.todo.os.ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN;
import static java.util.stream.Collectors.toMap;

@Command(name = "times")
public final class TimesCommand implements Runnable {
	private final Tasks tasks;
	private final OSInterface osInterface;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@Option(names = {"--log"})
	private boolean log;

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

		long idSpace = Long.toString(maxID).length();

		Utils.HighestTime highestTime = getHighestTime(filter);

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

				long listTime = 0;
				for (TaskTimesFilter.TaskTimeFilterResult task : taskTimeFilterResults) {
					listTime += task.total;
				}

				System.out.println();
				System.out.print(ConsoleColors.ANSI_BOLD);
				System.out.print(Utils.formatTime(listTime, highestTime));
				System.out.print(" ");
				System.out.print(taskTimeFilterResults.get(0).list);
				System.out.print(ANSI_RESET);
				System.out.println();
				totalTime += printResults(taskTimeFilterResults, highestTime, idSpace);
			}

			System.out.println();
			System.out.print(Utils.formatTime(totalTime, highestTime));
		}
		else {
			results.sort(Comparator.comparingLong(TaskTimesFilter.TaskTimeFilterResult::getTotal).reversed());

			long totalTime = printResults(results, highestTime, idSpace);

			System.out.println();
			System.out.print(Utils.formatTime(totalTime, highestTime));
		}
		System.out.print("   Total");
		System.out.println();
		System.out.println();
	}

	private Utils.HighestTime getHighestTime(TaskTimesFilter filter) {
		long totalTime = filter.getData().stream()
				.map(TaskTimesFilter.TaskTimeFilterResult::getTotal)
				.reduce(0L, Long::sum);

		return Utils.fromTimestamp(totalTime);
	}

	private long printResults(List<TaskTimesFilter.TaskTimeFilterResult> data, Utils.HighestTime highestTime, long idSpace) {
		long totalTime = 0;

		for (TaskTimesFilter.TaskTimeFilterResult result : data) {
			String line = Utils.formatTime(result.getTotal(), highestTime);

			Task task = result.getTask();

			boolean active = tasks.getActiveTaskID() == task.id;

			if (active) {
				line += " * ";
				line += ANSI_FG_GREEN;
			}
			else if (task.state == TaskState.Finished) {
				line += " F ";
			}
			else if (task.isRecurring()) {
				line += " R ";
			}
			else {
				line += "   ";
			}

			line += task.description(idSpace);

			int length = line.length();

			if (active) {
				length -= ANSI_FG_GREEN.toString().length();
			}

			if (length > osInterface.getTerminalWidth()) {
				line = line.substring(0, osInterface.getTerminalWidth() - 4 + (line.length() - length));
				line += "...'";
			}

			if (active) {
				line += ANSI_RESET;
			}

			System.out.println(line);

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
		else if (log) {
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

	private static class InfoData implements Comparable<InfoData> {
		final long time;
		private final Type type;
		final String message;

		enum Type {
			Add,
			Start,
			Stop,
			Finish
		}
		private InfoData(long time, Type type, String message) {
			this.time = time;
			this.type = type;
			this.message = message;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			InfoData infoData = (InfoData) o;
			return time == infoData.time &&
					type == infoData.type &&
					message.equals(infoData.message);
		}

		@Override
		public int hashCode() {
			return Objects.hash(time, type, message);
		}

		@Override
		public int compareTo(InfoData o) {
			if (time == o.time) {
				return type.compareTo(o.type);
			}
			return Long.compare(time, o.time);
		}
	}

	private void displayLog(TaskTimesFilter filter) {
		List<InfoData> data = new ArrayList<>();

		for (TaskTimesFilter.TaskTimeFilterResult result : filter.getData()) {
			data.add(new InfoData(result.task.getAddTime().start, InfoData.Type.Add, "Added " + result.task.description()));

			for (final TaskTimes startStopTime : result.task.getStartStopTimes()) {
				data.add(new InfoData(startStopTime.start, InfoData.Type.Start, "Started " + result.task.description()));

				if (startStopTime.stop != TaskTimes.TIME_NOT_SET) {
					data.add(new InfoData(startStopTime.stop, InfoData.Type.Stop, "Stopped " + result.task.description()));
				}
			}

			if (result.task.getFinishTime().isPresent()) {
				data.add(new InfoData(result.task.getFinishTime().get().start, InfoData.Type.Finish, "Finished " + result.task.description()));
			}
		}

		data.sort(Comparator.naturalOrder());

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a");

		ZoneId zoneId = osInterface.getZoneId();

		for (final InfoData infoData : data) {
			Instant instant = Instant.ofEpochSecond(infoData.time);

			System.out.print(instant.atZone(zoneId).format(dateTimeFormatter));
			System.out.print("   ");
			System.out.println(infoData.message);
		}
	}

	private void displayProjectsFeatures(TaskTimesFilter filter) {
		Map<ProjFeatOutput, Long> outputs = new HashMap<>();

		long totalTime = 0;
		int longestProject = 0;

		Utils.HighestTime highestTime = getHighestTime(filter);

		for (TaskTimesFilter.TaskTimeFilterResult task : filter.getData()) {
			String project = tasks.getProjectForTask(task.task.id);
			String feature = tasks.getFeatureForTask(task.task.id);

			if (project.isEmpty()) {
				project = "None";
			}
			if (feature.isEmpty()) {
				feature = "None";
			}

			totalTime += task.total;

			ProjFeatOutput output = new ProjFeatOutput(project, feature);

			outputs.put(output, outputs.getOrDefault(output, 0L) + task.total);

			if (project.length() > longestProject) {
				longestProject = project.length();
			}
		}

		int longestTime = Utils.formatTime(totalTime, highestTime).length();

		LinkedHashMap<ProjFeatOutput, Long> collect = outputs.entrySet()
				.stream()
				.sorted(Map.Entry.<ProjFeatOutput, Long>comparingByValue().reversed())
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

		System.out.print(String.format("%-" + longestTime + "s", "Time"));
		System.out.print("   ");
		System.out.print(String.format("%-" + longestProject + "s", "Project"));
		System.out.print("   Feature");
		System.out.println();
		System.out.println();

		for (ProjFeatOutput output : collect.keySet()) {
			long time = outputs.get(output);

			System.out.print(Utils.formatTime(time, highestTime));
			System.out.print("   ");
			if (output.project.equals("None")) {
				System.out.print(ANSI_REVERSED + "None" + ANSI_RESET);
			}
			else {
				System.out.print(output.project);
			}
			System.out.print(String.join("", Collections.nCopies(longestProject - output.project.length(), " ")));
			System.out.print("   ");
			if (output.feature.equals("None")) {
				System.out.println(ANSI_REVERSED + "None" + ANSI_RESET);
			}
			else {
				System.out.println(output.feature);
			}
		}

		System.out.println();
		System.out.print(Utils.formatTime(totalTime, highestTime));
		System.out.print("   Total");
		System.out.println();
		System.out.println();
	}

	public static final class ProjFeatOutput {
		final String project;
		final String feature;

		ProjFeatOutput(String project, String feature) {
			this.project = project;
			this.feature = feature;
		}

		@Override
		public int hashCode() {
			return Objects.hash(project, feature);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			ProjFeatOutput that = (ProjFeatOutput) o;
			return Objects.equals(project, that.project) &&
					Objects.equals(feature, that.feature);
		}
	}
}
