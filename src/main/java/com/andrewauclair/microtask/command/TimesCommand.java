// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.ConsoleTable;
import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.Utils;
import com.andrewauclair.microtask.jline.GroupCompleter;
import com.andrewauclair.microtask.jline.ListCompleter;
import com.andrewauclair.microtask.os.ConsoleColors;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.project.Projects;
import com.andrewauclair.microtask.task.*;
import com.andrewauclair.microtask.task.group.name.ExistingGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
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

import static com.andrewauclair.microtask.ConsoleTable.Alignment.LEFT;
import static com.andrewauclair.microtask.ConsoleTable.Alignment.RIGHT;
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_REVERSED;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_GREEN;
import static java.util.stream.Collectors.toMap;

// TODO add new 'times by-category' subcommand that deals with showing tasks grouped and counted by their time category (pulled from groups and lists) not going to store it per start/stop like originally
@Command(name = "times", synopsisSubcommandLabel = "COMMAND", description = "Display times for tasks, lists or groups.")
public final class TimesCommand implements Runnable {
	private final Tasks tasks;
	private final Projects projects;
	private final OSInterface osInterface;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@Option(names = {"--proj-feat"}, description = "Display times grouped as projects and features.")
	private boolean proj_feat;

	@Option(names = {"--list"}, completionCandidates = ListCompleter.class, description = "The list to display times for.")
	private ExistingListName[] list;

	@Option(names = {"--group"}, completionCandidates = GroupCompleter.class, description = "The group to display times for.")
	private ExistingGroupName[] group;

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

	@Option(names = {"-v", "--verbose"}, description = "Display times with verbose output.")
	private boolean verbose;

	TimesCommand(Tasks tasks, Projects projects, OSInterface osInterface) {
		this.tasks = tasks;
		this.projects = projects;
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

		ConsoleTable table = new ConsoleTable(osInterface);
		table.enableAlternatingColors();
		table.setColumnAlignment(RIGHT, LEFT, RIGHT, LEFT);
		table.setHeaders("Time", "Type", "ID", "Description");

		Utils.HighestTime highestTime = getHighestTime(filter);

		long totalTime = 0;

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


			if (!verbose) {
				System.out.println();

				for (List<TaskTimesFilter.TaskTimeFilterResult> taskTimeFilterResults : lists) {
					taskTimeFilterResults.sort(Comparator.comparingLong(TaskTimesFilter.TaskTimeFilterResult::getTotal).reversed());

					long listTime = 0;
					for (TaskTimesFilter.TaskTimeFilterResult task : taskTimeFilterResults) {
						listTime += task.total;
					}

					if (!verbose) {
						System.out.print(ConsoleColors.ANSI_BOLD);
						System.out.print(Utils.formatTime(listTime, highestTime));
						System.out.print(" ");
						System.out.print(taskTimeFilterResults.get(0).list);
						System.out.println(ANSI_RESET);
					}

					if (verbose) {
						totalTime += printResults(table, taskTimeFilterResults, highestTime);

						if (!total) {
							table.print();
						}
					}
					else {
						for (final TaskTimesFilter.TaskTimeFilterResult result : taskTimeFilterResults) {
							totalTime += result.getTotal();
						}
					}
				}
			}
			else {
				List<TaskTimesFilter.TaskTimeFilterResult> allListResults = new ArrayList<>();
				for (final List<TaskTimesFilter.TaskTimeFilterResult> taskTimeFilterResults : lists) {
					allListResults.addAll(taskTimeFilterResults);
				}

				allListResults.sort(Comparator.comparingLong(TaskTimesFilter.TaskTimeFilterResult::getTotal).reversed());

				System.out.println();

				totalTime += printResults(table, allListResults, highestTime);

				if (!total) {
					table.print();
				}
			}
		}
		else {
			results.sort(Comparator.comparingLong(TaskTimesFilter.TaskTimeFilterResult::getTotal).reversed());

			totalTime = printResults(table, results, highestTime);

			if (!total) {
				table.print();
			}

		}
		System.out.println();
//			System.out.print(Utils.formatTime(totalTime, highestTime));

		System.out.print(String.format("%-" + table.getColumnWidths().get(0) + "s   Total", Utils.formatTime(totalTime, highestTime)));

//		System.out.print("  Total");
		System.out.println();
		System.out.println();
	}

	private Utils.HighestTime getHighestTime(TaskTimesFilter filter) {
		long totalTime = filter.getData().stream()
				.map(TaskTimesFilter.TaskTimeFilterResult::getTotal)
				.reduce(0L, Long::sum);

		return Utils.fromTimestamp(totalTime);
	}

	private long printResults(ConsoleTable table, List<TaskTimesFilter.TaskTimeFilterResult> data, Utils.HighestTime highestTime) {
		long totalTime = 0;

		for (TaskTimesFilter.TaskTimeFilterResult result : data) {
			String time = Utils.formatTime(result.getTotal(), highestTime);

			String type = "";
			Task task = result.task;

			boolean active = tasks.getActiveTaskID() == task.ID().get().ID();

			if (active) {
				type += "*";
			}
			else {
				type += " ";
			}

			if (task.recurring) {
				type += "R";
			}
			else {
				type += " ";
			}

			if (task.state == TaskState.Finished) {
				type += "F";
			}
			else {
				type += " ";
			}

			if (!total) {
				if (active) {
					table.addRow(ANSI_BG_GREEN, false, time, type, String.valueOf(task.ID()), task.task);
				}
				else {
					table.addRow(time, type, String.valueOf(task.ID()), task.task);
				}
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
		else if (all_month) {
			filter.filterForMonth(month);
		}

		if ((list != null || group != null) && this.day == null && !today) {
			List<ExistingListName> lists = new ArrayList<>();

			if (list != null) {
				lists.addAll(Arrays.asList(list));
			}

			if (group != null) {
				for (ExistingGroupName group : group) {
					lists.addAll(tasks.getGroup(group.absoluteName()).getChildren().stream()
							.filter(child -> child instanceof TaskList)
							.map(TaskContainer::getFullPath)
							.map(path -> new ExistingListName(tasks, path))
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
			if (all_time || all_month || today || yesterday || week || this.day != null) {
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

//				filter.filterForMonth(month);

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

	private void displayProjectsFeatures(TaskTimesFilter filter) {
		Map<ProjFeatOutput, Long> outputs = new HashMap<>();

		long totalTime = 0;
		int longestProject = 1;

		Utils.HighestTime highestTime = getHighestTime(filter);

		for (TaskTimesFilter.TaskTimeFilterResult task : filter.getData()) {
			String project = projects.getProjectForList(tasks.findListForTask(task.task.ID()));
//			String feature = getFeatureForTask(new ExistingID(tasks, task.task.id));
			String feature = projects.getFeatureForList(tasks.getListForTask(task.task.ID()));

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
