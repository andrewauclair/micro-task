// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.Tasks;

import java.io.IOException;
import java.util.Properties;

public class LocalSettings {
	private final OSInterface osInterface;

	private String activeList = "/default";
	private String activeGroup = "/";

	public LocalSettings(OSInterface osInterface) {
		this.osInterface = osInterface;
	}

	public String getActiveList() {
		return activeList;
	}

	public void setActiveList(String activeList) {
		this.activeList = activeList;

		save();
	}

	public String getActiveGroup() {
		return activeGroup;
	}

	public void setActiveGroup(String activeGroup) {
		this.activeGroup = activeGroup;

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

		tasks.setActiveList(activeList);
		tasks.switchGroup(activeGroup);
	}

	private void save() {
		Properties properties = new Properties();
		properties.setProperty("active_list", activeList);
		properties.setProperty("active_group", activeGroup);

		try {
			properties.store(osInterface.createOutputStream("settings.properties"), "");
		}
		catch (IOException e) {
			e.printStackTrace(System.out);
		}
	}
}
