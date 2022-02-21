// Copyright (C) 2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.ExistingID;
import com.andrewauclair.microtask.task.TaskTimesFilter;
import com.andrewauclair.microtask.task.Tasks;
import picocli.CommandLine;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

@CommandLine.Command(name = "tracking")
public class TimesTrackingCommand implements Runnable {
	private final Tasks tasks;
	private final OSInterface osInterface;

	@CommandLine.Option(names = {"-d", "--day"}, description = "Day to display times for.")
	private Integer day;

	@CommandLine.Option(names = {"-m", "--month"}, description = "Month to display times for.")
	private Integer month;

	@CommandLine.Option(names = {"-y", "--year"}, description = "Year to display times for.")
	private Integer year;

	TimesTrackingCommand(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}

	private static class Times {
		double[] times = new double[7];
		double week_total = 0;

		void addTime(int i, double time) {
			times[i] += time;
			calculateTotal();
		}

		void calculateTotal() {
			week_total = 0;
			for (int i = 0; i < 7; i++) {
				week_total += times[i];
			}
		}

		double timeForDay(int i) {
			return times[i];
		}

		double weekTotal() {
			return week_total;
		}
	}

	@Override
	public void run() {
		Instant start_instant = Instant.ofEpochSecond(osInterface.currentSeconds());

		if (this.day != null) {
			ZoneId zoneId = osInterface.getZoneId();

			if (this.day < 1 || this.day > 31) {
				throw new TaskException("Day option must be 1 - 31");
			}

			if (this.month != null && (this.month < 1 || this.month > 12)) {
				throw new TaskException("Month option must be 1 - 12");
			}

			day = this.day;
			month = this.month != null ? this.month : start_instant.atZone(zoneId).getMonthValue();
			year = this.year != null ? this.year : start_instant.atZone(zoneId).getYear();

			LocalDate of = LocalDate.of(year, month, day);

			start_instant = of.atStartOfDay(zoneId).toInstant();
		}

		LocalDate weekDay = LocalDate.ofInstant(start_instant, osInterface.getZoneId());

		start_instant = weekDay.minusDays(weekDay.getDayOfWeek().getValue()).atStartOfDay(osInterface.getZoneId()).toInstant();

		LocalDate start_date = LocalDate.ofInstant(start_instant, osInterface.getZoneId());

		Map<String, Times> times = new HashMap<>();

		for (int i = 0; i < 7; i++) {
			TaskTimesFilter filter = tasks.getFilterBuilder().createFilter(tasks);

			Instant instant = start_instant.plus(86_400 * i, ChronoUnit.SECONDS);

			LocalDate currentDate = LocalDate.ofInstant(instant, osInterface.getZoneId());

			int day = currentDate.getDayOfMonth();
			int month = currentDate.getMonth().getValue();
			int year = currentDate.getYear();

			filter.filterForDay(month, day, year);

			List<TaskTimesFilter.TaskTimeFilterResult> data = filter.getData();

			for (final TaskTimesFilter.TaskTimeFilterResult datum : data) {
				String charge = tasks.getListForTask(new ExistingID(tasks, datum.task.id)).getTimeCategory();

				if (!times.containsKey(charge)) {
					times.put(charge, new Times());
				}

				double hours = datum.total / 900.0;

				times.get(charge).addTime(i, hours);
			}
		}

		System.out.println("Time Tracking for Week of " + String.format("%d/%d/%d", start_date.getMonth().getValue(), start_date.getDayOfMonth(), start_date.getYear()));
		System.out.println();

		double[] totals = new double[7];

		int longest = 0;
		for (final String s : times.keySet()) {
			if (s.length() > longest) {
				longest = s.length();
			}
		}

		final int lone = longest;

		System.out.print(String.join("", Collections.nCopies(lone + 3, " ")));

		System.out.println("   Su      Mo      Tu      We      Th      Fr      Sa      Total");

		DecimalFormat format = new DecimalFormat("#0.00");

		times.entrySet().stream()
				.sorted(((Comparator<Map.Entry<String, Times>>) (o1, o2) -> Double.compare(o1.getValue().weekTotal(), o2.getValue().weekTotal())).reversed())
				.forEach(stringTimesEntry -> {
			System.out.printf("%-" + lone + "s", stringTimesEntry.getKey());

			double total = 0;

			for (int j = 0; j < 7; j++) {
				double aDouble = stringTimesEntry.getValue().timeForDay(j);

				if (aDouble == 0) {
					System.out.print("        ");
				}
				else {
					aDouble = Math.ceil(aDouble * 4) / 4;
					System.out.printf("  %5sh", format.format(aDouble));
				}
				totals[j] += aDouble;
				total += aDouble;
			}

			System.out.printf("     %5sh", format.format(total));
			System.out.println();
		});
		System.out.println();

		double week_total = 0;

		for (final double total : totals) {
			week_total += total;
		}

		System.out.print("Total");
		System.out.print(String.join("", Collections.nCopies(lone - 5, " ")));

		for (int i = 0; i < 7; i++) {
			if (totals[i] == 0) {
				System.out.print("        ");
			}
			else {
				System.out.printf("  %5sh", format.format(totals[i]));
			}
		}
		System.out.printf("     %5sh%n", format.format(week_total));
		System.out.println();
	}
}
