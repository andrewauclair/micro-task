// Copyright (C) 2019 Andrew Auclair - All Rights Reserved

import java.util.ArrayList;
import java.util.List;

class Tasks {
	private static List<String> tasks = new ArrayList<>();
	
	static int addTask(String task) {
		tasks.add(task);
		return 0;
	}
	
	static void startTask(int id) {
	
	}
	
	static int getActiveTask() {
		return -1;
	}
	
	static List<String> getTasks() {
		return tasks;
	}
}
