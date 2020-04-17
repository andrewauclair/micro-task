// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.Utils;
import com.andrewauclair.microtask.jline.GroupCompleter;
import com.andrewauclair.microtask.jline.ListCompleter;
import com.andrewauclair.microtask.os.ConsoleColors;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.*;
import com.andrewauclair.microtask.task.group.name.ExistingTaskGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingTaskListName;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_REVERSED;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN;
import static java.util.stream.Collectors.toMap;

@Command(name = "times", description = "Display times for tasks, lists or groups.")
public final class TimesCommand implements Runnable {
	private final Tasks tasks;
	private final OSInterface osInterface;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@Option(names = {"--proj-feat"}, description = "Display times grouped as projects and features.")
	private boolean proj_feat;

	@Option(names = {"--list"}, completionCandidates = ListCompleter.class, description = "The list to display times for.")
	private ExistingTaskListName[] list;

	@Option(names = {"--group"}, completionCandidates = GroupCompleter.class, description = "The group to display times for.")
	private ExistingTaskGroupName[] group;

	@Option(names = {"--today"}, description = "Display times for today.")
	private boolean today;

	@Option(names = {"--yesterday"}, description = "Display times for yesterday.")
	private boolean yesterday;

	@Option(names = {"-d", "--day"}, description = "Day to display times for.")
	private Integer day;

	@Option(names = {"-m", "--month"}, description = "Month to display times for.")
	private Integer month;

	@Option(names = {"-y", "--year"}, description = "Year to display times for.")
	private Integer year;

	@Option(names = {"--week"}, description = "Week to display times for.")
	private boolean week;

	@Option(names = {"--all-month"}, description = "Display times for the entire month")
	private boolean all_month;

	@Option(names = {"--all-time"}, description = "Display all task times recorded.")
	private boolean all_time;

	@Option(names = {"--total"}, description = "Display only the final total.")
	private boolean total;

	TimesCommand(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}

	private void displayTimesForDay(Instant day, TaskTimesFilter filter) {
		if (total) {
			System.out.print("Total time for day ");
		}
		else {
			System.out.print("Times for day ");
		}

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

		ZoneId zoneId = osInterface.getZoneId();

		System.out.println(day.atZone(zoneId).format(dateTimeFormatter));

		boolean printListNames = list != null;

		if (!printListNames && !total) {
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

				if (!total) {
					System.out.println();
				}
				totalTime += printResults(taskTimeFilterResults, highestTime, idSpace);
			}

			if (total) {
				System.out.println();
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

			Task task = result.task;

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

			if (!total) {
				System.out.println(line);
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
			Arrays.stream(list).forEach(list -> filter.filterForList(list.absoluteName()));
		}

		if (group != null) {
			Arrays.stream(group).forEach(group -> filter.filterForGroup(tasks.getGroup(group.absoluteName())));
		}

		Instant instant = Instant.ofEpochSecond(osInterface.currentSeconds());

		LocalDate currentDate = LocalDate.ofInstant(instant, osInterface.getZoneId());

		int day = currentDate.getDayOfMonth();
		int month = currentDate.getMonth().getValue();
		int year = currentDate.getYear();

		if (this.day != null) {
			ZoneId zoneId = osInterface.getZoneId();

			if (this.day < 1 || this.day > 31) {
				throw new TaskException("Day option must be 1 - 31");
			}

			if (this.month != null && (this.month < 1 || this.month > 12)) {
				throw new TaskException("Month option must be 1 - 12");
			}

			day = this.day;
			month = this.month != null ? this.month : instant.atZone(zoneId).getMonthValue();
			year = this.year != null ? this.year : instant.atZone(zoneId).getYear();

			LocalDate of = LocalDate.of(year, month, day);

			instant = of.atStartOfDay(zoneId).toInstant();
		}

		if (week) {
			LocalDate weekDay = LocalDate.ofInstant(instant, osInterface.getZoneId());

			instant = weekDay.minusDays(weekDay.getDayOfWeek().getValue()).atStartOfDay(osInterface.getZoneId()).toInstant();

			filter.filterForWeek(month, day, year);
		}
		else if (today) {
			filter.filterForDay(currentDate.getMonth().getValue(), currentDate.getDayOfMonth(), currentDate.getYear());
		}
		else if (yesterday) {
			long epochSecond = osInterface.currentSeconds() - (60 * 60 * 24);

			instant = Instant.ofEpochSecond(epochSecond);

			LocalDate yesterday = LocalDate.ofInstant(instant, osInterface.getZoneId());

			filter.filterForDay(yesterday.getMonth().getValue(), yesterday.getDayOfMonth(), yesterday.getYear());
		}
		else if (this.day != null) {
			filter.filterForDay(month, day, year);
		}

		if ((list != null || group != null) && this.day == null && !today) {
			List<ExistingTaskListName> lists = new ArrayList<>();

			if (list != null) {
				lists.addAll(Arrays.asList(list));
			}

			if (group != null) {
				for (ExistingTaskGroupName group : group) {
					lists.addAll(tasks.getGroup(group.absoluteName()).getChildren().stream()
							.filter(child -> child instanceof TaskList)
							.map(TaskContainer::getFullPath)
							.map(path -> new ExistingTaskListName(tasks, path))
							.collect(Collectors.toSet()));
				}
			}

			if (list != null && lists.size() > 1) {
				if (total) {
					System.out.println("Total times for multiple lists");
				}
				else {
					System.out.println("Times for multiple lists");
				}
			}
			else if (group != null && group.length > 1) {
				if (total) {
					System.out.println("Total times for multiple groups");
				}
				else {
					System.out.println("Times for multiple groups");
				}
			}
			else if (group != null) {
				System.out.println("Times for group '" + group[0] + "'");
			}
			else {
				String list = lists.get(0).absoluteName();//tasks.getAbsoluteListName(lists.get(0));

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
		else if (today && !proj_feat) {
			displayTimesForDay(Instant.ofEpochSecond(osInterface.currentSeconds()), filter);
		}
		else if (yesterday && !proj_feat) {
			displayTimesForDay(instant, filter);
		}
		else if (week && !proj_feat) {
			if (total) {
				System.out.print("Total times for week of ");
			}
			else {
				System.out.print("Times for week of ");
			}

			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

			ZoneId zoneId = osInterface.getZoneId();

			System.out.println(instant.atZone(zoneId).format(dateTimeFormatter));

			if (!total) {
				System.out.println();
			}

			displayTimes(filter, false);
		}
		else if (proj_feat) {
			if (all_time || today || yesterday || week || this.day != null) {
				displayProjectsFeatures(filter);
			}
			else {
				System.out.println("Invalid command.");
				System.out.println();
			}
		}
		else {
			if (all_time) {
				if (total) {
					System.out.println("Total times");
				}
				else {
					System.out.println("Times");
					System.out.println();
				}
				printTasks(new TaskTimesFilter(tasks));
			}
			else if (all_month) {
				String title = Month.of(month).getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + year;

				filter.filterForMonth(month);

				if (total) {
					System.out.print("Total times for month of ");
					System.out.println(title);
				}
				else {
					System.out.print("Times for month of ");
					System.out.println(title);
					System.out.println();
				}
				printTasks(filter);
			}
			else if (this.day != null) {
				displayTimesForDay(instant, filter);
			}
			else {
				System.out.println("Invalid command.");
				System.out.println();
			}
		}
	}

	public String getFeatureForTask(ExistingID taskID) {
		TaskList listForTask = tasks.findListForTask(taskID);

		String feature = listForTask.getFeature();

		TaskGroup group = tasks.getGroupForList(new ExistingTaskListName(tasks, listForTask.getFullPath()));

		while (group != null) {
			if (feature.isEmpty()) {
				feature = group.getFeature();
			}
			else if (!group.getFeature().isEmpty()) {
				feature = group.getFeature() + " " + feature;
			}

			if (group.getFullPath().equals("/")) {
				break;
			}
			if (!group.getParent().equals("/")) {
				group = tasks.getGroup(group.getParent());
			}
			else {
				group = null;
			}
		}
		return feature;
	}

	private void displayProjectsFeatures(TaskTimesFilter filter) {
		Map<ProjFeatOutput, Long> outputs = new HashMap<>();

		long totalTime = 0;
		int longestProject = 1;

		Utils.HighestTime highestTime = getHighestTime(filter);

		for (TaskTimesFilter.TaskTimeFilterResult task : filter.getData()) {
			String project = new TaskFinder(tasks).getProjectForTask(new ExistingID(tasks, task.task.id));
			String feature = getFeatureForTask(new ExistingID(tasks, task.task.id));

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

		int longestTime = Math.max(Utils.formatTime(totalTime, highestTime).length(), 1);

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
