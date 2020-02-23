// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.Utils;
import com.andrewauclair.todo.os.OSInterface;
import com.andrewauclair.todo.task.TaskTimesFilter;
import com.andrewauclair.todo.task.Tasks;
import picocli.CommandLine;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@CommandLine.Command(name = "eod")
class EndOfDayCommand extends Command {
	@CommandLine.Option(names = {"--hours"})
	private Integer hours;

	private final Tasks tasks;
	private final OSInterface osInterface;

	EndOfDayCommand(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		long epochSecond = osInterface.currentSeconds();

		Instant instant = Instant.ofEpochSecond(epochSecond);

		ZoneId zoneId = osInterface.getZoneId();

		LocalDate today = LocalDate.ofInstant(instant, zoneId);

		List<TaskTimesFilter.TaskTimeFilterResult> data = new TaskTimesFilter(this.tasks).filterForDay(today.getMonth().getValue(), today.getDayOfMonth(), today.getYear()).getData();

		// total the task times and determine how much time is left, then add that to the current seconds
		long totalTime = 0;

		for (TaskTimesFilter.TaskTimeFilterResult filterResult : data) {
			totalTime += filterResult.getTotal();
		}

		int hours = this.hours != null ? this.hours : 8;
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

		System.out.println();
	}
}
