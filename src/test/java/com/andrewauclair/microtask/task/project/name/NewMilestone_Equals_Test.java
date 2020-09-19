// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task.project.name;

import com.andrewauclair.microtask.project.NewMilestone;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

public class NewMilestone_Equals_Test {
	@Test
	void new_milestone_equals() {
		EqualsVerifier.forClass(NewMilestone.class)
				.verify();
	}
}
