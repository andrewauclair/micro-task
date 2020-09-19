// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task.project.name;

import com.andrewauclair.microtask.project.ExistingMilestone;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

public class ExistingMilestone_Equals_Test {
	@Test
	void existing_milestone_equals() {
		EqualsVerifier.forClass(ExistingMilestone.class)
				.verify();
	}
}
