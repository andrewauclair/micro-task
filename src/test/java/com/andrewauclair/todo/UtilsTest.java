// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.stream.Stream;

import static com.andrewauclair.todo.Utils.NL;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilsTest {
	@Test
	void create_a_utils_instance_to_satisfy_codecov() {
		new Utils();
	}
	
	static Stream<Arguments> timeFormatSource() {
		return Stream.of(
				Arguments.of(" 0s", 0, Utils.HighestTime.None),
				Arguments.of(" 5s", 5, Utils.HighestTime.None),
				Arguments.of(" 8m  5s", 485, Utils.HighestTime.None),
				Arguments.of("7h  8m  5s", 25_685, Utils.HighestTime.None),
				Arguments.of("3d 1h  8m  5s", 90_485, Utils.HighestTime.None),
				Arguments.of(" 1w 3d 1h  8m  5s", 234_485, Utils.HighestTime.None),
				
				Arguments.of(" 5s", 5, Utils.HighestTime.Second),
				Arguments.of("     5s", 5, Utils.HighestTime.Minute),
				Arguments.of("        5s", 5, Utils.HighestTime.Hour),
				Arguments.of("           5s", 5, Utils.HighestTime.Day),
				Arguments.of("               5s", 5, Utils.HighestTime.Week),
				
				Arguments.of(" 1w 0d 0h  0m  0s", 144_000, Utils.HighestTime.None),
				Arguments.of("1d 0h  0m  0s", 28_800, Utils.HighestTime.None),
				Arguments.of("1h  0m  0s", 3_600, Utils.HighestTime.None),
				Arguments.of(" 1m  0s", 60, Utils.HighestTime.None),
				Arguments.of("15w 4d 6h 12m 11s", (144_000 * 15) + (28_800 * 4) + (3_600 * 6) + (60 * 12) + 11, Utils.HighestTime.None)
		);
	}
	
	@ParameterizedTest
	@MethodSource("timeFormatSource")
	void test(String output, long time, Utils.HighestTime highestTime) {
		assertEquals(output, Utils.formatTime(time, highestTime));
	}

	public static String createFile(String... lines) {
		StringBuilder buffer = new StringBuilder();

		for (String line : lines) {
			buffer.append(line);
			buffer.append(NL);
		}

		return buffer.toString();
	}

	public static ByteArrayInputStream byteInStream(String data) {
		return new ByteArrayInputStream(data.getBytes());
	}
}
