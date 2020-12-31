// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.description.TextDescription;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Copyright_Test {
	@Test
	void all_java_files_have_copyright() throws FileNotFoundException {
		File folder = new File("src");

		SoftAssertions soft = new SoftAssertions();

		checkFolder(soft, folder);

		soft.assertAll();
	}

	private void checkFolder(SoftAssertions soft, File folder) throws FileNotFoundException {
		if (folder.listFiles() == null) {
			return;
		}

		for (final File file : folder.listFiles()) {
			if (file.isDirectory()) {
				checkFolder(soft, file);
			}
			else if (file.getName().endsWith(".java")) {
				try (Scanner scanner = new Scanner(file)) {
					soft.assertThat(scanner.nextLine()).describedAs(new TextDescription(file.getAbsolutePath())).startsWith("//");
				}
			}
		}
	}
}
