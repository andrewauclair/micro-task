// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class Task_Equals_Test {
	@Test
	void task_equals_task() {
		EqualsVerifier.forClass(Task.class)
				.withIgnoredFields("shortID") // ignore shortID, it is mutable so that we don't have to recreate all tasks when shortIDs get reassigned
				.verify();
	}

	@Test
	void full_id_equals() {
		EqualsVerifier.forClass(FullTaskID.class)
				.verify();
	}

	@Test
	void relative_id_equals() {
		EqualsVerifier.forClass(RelativeTaskID.class)
				.verify();
	}
}
