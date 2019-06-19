// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.os.OSInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskBaseTestCase {
	final TaskWriter writer = Mockito.mock(TaskWriter.class);
	final OSInterface osInterface = Mockito.mock(OSInterface.class);
	final Tasks tasks = new Tasks(writer, osInterface);
	
	@BeforeEach
	void setup() {
		Mockito.when(osInterface.runGitCommand(Mockito.any())).thenReturn(true);
	}
}
