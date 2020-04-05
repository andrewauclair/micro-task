package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.task.TaskTimesFilter;
import com.andrewauclair.microtask.task.TaskFilterBuilder;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Commands_Times_BaseTestCase extends CommandsBaseTestCase {
	private static final long SECONDS_IN_DAY = 86400;
	private static final long MINUTE = 60;
	private static final long HOUR = 60 * MINUTE;

	protected final long june17_8_am = 1560772800;
	final long june18_8_am = june17_8_am + SECONDS_IN_DAY;
	
	protected final TaskFilterBuilder mockTaskFilterBuilder = mock(TaskFilterBuilder.class);
	protected final TaskTimesFilter mockTaskTimesFilter = mock(TaskTimesFilter.class);
	
	@BeforeEach
	public void setup() throws IOException {
		super.setup();
		
		when(mockTaskFilterBuilder.createFilter(tasks)).thenReturn(mockTaskTimesFilter);
		
		tasks.setFilterBuilder(mockTaskFilterBuilder);
	}
}
