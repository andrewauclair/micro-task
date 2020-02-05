// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilsTest {
	@Test
	void create_a_utils_instance_to_satisfy_codecov() {
		new Utils();
	}
	
	static Stream<Arguments> timeFormatSource() {
		return Stream.of(
				Arguments.of("00s", 0, Utils.HighestTime.None),
				Arguments.of("05s", 5, Utils.HighestTime.None),
				Arguments.of("08m 05s", 485, Utils.HighestTime.None),
				Arguments.of("07h 08m 05s", 25_685, Utils.HighestTime.None),
				Arguments.of("03d 01h 08m 05s", 90_485, Utils.HighestTime.None),
				Arguments.of("01w 03d 01h 08m 05s", 234_485, Utils.HighestTime.None),
				
				Arguments.of("05s", 5, Utils.HighestTime.Second),
				Arguments.of("00m 05s", 5, Utils.HighestTime.Minute),
				Arguments.of("00h 00m 05s", 5, Utils.HighestTime.Hour),
				Arguments.of("00d 00h 00m 05s", 5, Utils.HighestTime.Day),
				Arguments.of("00w 00d 00h 00m 05s", 5, Utils.HighestTime.Week),
				
				Arguments.of("01w 00d 00h 00m 00s", 144_000, Utils.HighestTime.None),
				Arguments.of("01d 00h 00m 00s", 28_800, Utils.HighestTime.None),
				Arguments.of("01h 00m 00s", 3_600, Utils.HighestTime.None),
				Arguments.of("01m 00s", 60, Utils.HighestTime.None)
		);
	}
	
	@ParameterizedTest
	@MethodSource("timeFormatSource")
	void test(String output, long time, Utils.HighestTime highestTime) {
		assertEquals(output, Utils.formatTime(time, highestTime));
	}
}
