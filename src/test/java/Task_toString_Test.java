// Copyright (C) 2019 Andrew Auclair - All Rights Reserved

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Task_toString_Test {
	@Test
	void task_toString_displays_number_and_title() {
		assertEquals("1 - \"Test\"", new Tasks.Task(1, "Test").toString());
	}
}
