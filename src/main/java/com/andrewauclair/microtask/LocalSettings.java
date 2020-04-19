// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.group.name.ExistingTaskGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingTaskListName;

import java.io.IOException;
import java.util.Properties;

public class LocalSettings {
	private final OSInterface osInterface;

	private String activeList = "/default";
	private String activeGroup = "/";
	private boolean debugEnabled = false;
	private int hoursInDay = 8;

	public LocalSettings(OSInterface osInterface) {
		this.osInterface = osInterface;
	}

	public String getActiveList() {
		return activeList;
	}

	public void setActiveList(ExistingTaskListName activeList) {
		this.activeList = activeList.absoluteName();

		save();
	}

	public String getActiveGroup() {
		return activeGroup;
	}

	public void setActiveGroup(ExistingTaskGroupName activeGroup) {
		this.activeGroup = activeGroup.absoluteName();

		save();
	}

	public boolean isDebugEnabled() {
		return debugEnabled;
	}

	public void setDebugEnabled(boolean enabled) {
		debugEnabled = enabled;

		save();
	}

	public int hoursInDay() {
		return hoursInDay;
	}

	public void setHoursInDay(int hoursInDay) {
		this.hoursInDay = hoursInDay;

		save();
	}

	public void load(Tasks tasks) {
		Properties properties = new Properties();
		try {
			properties.load(osInterface.createInputStream("settings.properties"));
		}
		catch (IOException e) {
			e.printStackTrace(System.out);
		}

		activeList = properties.getProperty("active_list", "/default");
		activeGroup = properties.getProperty("active_group", "/");
		debugEnabled = Boolean.parseBoolean(properties.getProperty("debug", "false"));
		hoursInDay = Integer.parseInt(properties.getProperty("hours_in_day", "8"));

//		tasks.setActiveList(new TaskListName(tasks, activeList));
		tasks.setActiveList(new ExistingTaskListName(tasks, activeList));
		tasks.setActiveGroup(new ExistingTaskGroupName(tasks, activeGroup));
	}

	private void save() {
		Properties properties = new Properties();
		properties.setProperty("active_list", activeList);
		properties.setProperty("active_group", activeGroup);
		properties.setProperty("debug", String.valueOf(debugEnabled));
		properties.setProperty("hours_in_day", String.valueOf(hoursInDay));

		try {
			properties.store(osInterface.createOutputStream("settings.properties"), "");
		}
		catch (IOException e) {
			e.printStackTrace(System.out);
		}
	}
}
