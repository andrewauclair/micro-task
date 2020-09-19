// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class TestUtils {
	public static void assertOutput(OutputStream stream, String... lines) {
		StringBuilder output = new StringBuilder();

		for (int i = 0; i < lines.length; i++) {
			output.append(lines[i]);
			if (i + 1 < lines.length) {
				output.append(Utils.NL);
			}
		}

		assertThat(stream.toString()).isEqualTo(output.toString());
	}

	public static InputStream createInputStream(String... lines) {
		String content = String.join(Utils.NL, lines);

		return new ByteArrayInputStream(content.getBytes());
	}
}
