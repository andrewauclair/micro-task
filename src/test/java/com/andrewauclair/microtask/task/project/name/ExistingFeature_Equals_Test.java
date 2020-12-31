// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task.project.name;

import com.andrewauclair.microtask.project.ExistingFeature;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

public class ExistingFeature_Equals_Test {
	@Test
	void existing_feature_name_equals() {
		EqualsVerifier.forClass(ExistingFeature.class)
				.verify();
	}
}
