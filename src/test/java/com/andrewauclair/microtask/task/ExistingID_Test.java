// Copyright (C) 2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExistingID_Test {
	@Test
	void short_id_does_not_exist() {
		Tasks tasks = Mockito.mock(Tasks.class);
		IDValidator idValidator = Mockito.mock(IDValidator.class);

		Mockito.when(idValidator.containsShortID(new RelativeTaskID(1))).thenReturn(false);
		Mockito.when(tasks.idValidator()).thenReturn(idValidator);

		TaskException taskException = assertThrows(TaskException.class, () -> new ExistingID(tasks, -1));

		assertEquals("Task with relative ID 1 does not exist.", taskException.getMessage());
	}
}
