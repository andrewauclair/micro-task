// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.LocalSettings;
import com.andrewauclair.microtask.Utils;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.TaskTimes;
import com.andrewauclair.microtask.task.TaskTimesFilter;
import com.andrewauclair.microtask.task.Tasks;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Command(name = "day", description = "Print the start or end of the day.")
final class DayCommand implements Runnable {
	private final Tasks tasks;
	private final LocalSettings localSettings;
	private final OSInterface osInterface;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@Option(names = {"--start"}, description = "Display the start of the day.")
	private boolean start;

	@Option(names = {"--end"}, description = "Display the end of the day.")
	private boolean end;

	@Option(names = {"--hours"}, description = "Number of hours in the day.")
	private Integer hours;

	DayCommand(Tasks tasks, LocalSettings localSettings, OSInterface osInterface) {
		this.tasks = tasks;
		this.localSettings = localSettings;
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		long epochSecond = osInterface.currentSeconds();

		Instant instant = Instant.ofEpochSecond(epochSecond);

		ZoneId zoneId = osInterface.getZoneId();

		LocalDate today = LocalDate.ofInstant(instant, zoneId);
		LocalDateTime midnight = LocalDateTime.of(today, LocalTime.MIDNIGHT);
		long midnightStart = midnight.atZone(zoneId).toEpochSecond();

		List<TaskTimesFilter.TaskTimeFilterResult> data = new TaskTimesFilter(this.tasks).filterForDay(today.getMonth().getValue(), today.getDayOfMonth(), today.getYear()).getData();

		if (start) {
			if (data.size() > 0) {
				//		// exclude add and finish when finished
//		if (state == TaskState.Finished) {
//			return startStopTimes.subList(1, startStopTimes.size() - 1);
//		}
//		// exclude add
//		return startStopTimes.subList(1, startStopTimes.size());
				for (final TaskTimes startStopTime : data.get(0).task.startStopTimes) {
					if (startStopTime.start >= midnightStart) {
						DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
						String eodStr = Instant.ofEpochSecond(startStopTime.start).atZone(zoneId).format(dateTimeFormatter);

						System.out.println("Day started at " + eodStr);

						break;
					}
				}
			}
			else {
				System.out.println("Day not started.");
			}
		}
		else {

			// total the task times and determine how much time is left, then add that to the current seconds
			long totalTime = 0;

			for (TaskTimesFilter.TaskTimeFilterResult filterResult : data) {
				totalTime += filterResult.getTotal();
			}

			int hours = this.hours != null ? this.hours : localSettings.hoursInDay();
			long eod = epochSecond + ((hours * 3600) - totalTime);

			if (eod - epochSecond < 0) {
				System.out.println("Day complete.");
			}
			else {
				System.out.print("End of Day is in ");

				System.out.print(Utils.formatTime(eod - epochSecond, Utils.HighestTime.None));

				DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
				String eodStr = Instant.ofEpochSecond(eod).atZone(zoneId).format(dateTimeFormatter);

				System.out.print(" at ");
				System.out.println(eodStr);
			}
		}

		System.out.println();
	}
}
