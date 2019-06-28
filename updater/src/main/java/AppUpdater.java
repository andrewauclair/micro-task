// Copyright (C) 2019 Andrew Auclair - All Rights Reserved

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

class AppUpdater {
	public static void main(String[] args) throws IOException, InterruptedException {
		// run this to get the xml of the version
		// http://oss.sonatype.org/service/local/lucene/search?g=com.github.andrewauclair&a=todo-app

		// use the version to download the right jar, we'll rename it to todo-app.jar
		// http://oss.sonatype.org/service/local/artifact/maven/redirect?r=snapshots&g=com.github.andrewauclair&a=%ARTIFACT%&e=zip&v=%VERSION%

		ProcessBuilder pb = new ProcessBuilder(
				"curl",
				"-s",
				"https://oss.sonatype.org/service/local/lucene/search?g=com.github.andrewauclair&a=todo-app");

		pb.redirectErrorStream(true);
		Process p = pb.start();
		InputStream is = p.getInputStream();

		Scanner scanner = new Scanner(is);

		String snapshotVersion = "";

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();

			String latestSnapshot = "<latestSnapshot>";
			if (line.contains(latestSnapshot)) {
				snapshotVersion = line.substring(line.indexOf(latestSnapshot) + latestSnapshot.length(), line.indexOf("</latest"));
			}
		}

		System.out.println("Latest Snapshot: " + snapshotVersion);

		// Found a build, so now we can update to it
		File file = new File("todo-app.jar");

		if (file.exists() && !file.delete()) {
			System.out.println("Failed to delete old todo-app.jar");
			System.exit(-1);
		}

		// curl -o ~/%ARTIFACT%-%VERSION%.zip -L -#  "http://%HOST%/service/local/artifact/maven/redirect?r=%REPO%&g=%GROUP_ID%&a=%ARTIFACT%&e=zip&v=%VERSION%"
		pb = new ProcessBuilder(
				"curl",
				"-s",
				"-o",
				"todo-app.jar",
				"-L",
				"-#",
				"https://oss.sonatype.org/service/local/artifact/maven/redirect?r=snapshots&g=com.github.andrewauclair&a=todo-app&e=jar&v=" + snapshotVersion
		);

		pb.redirectErrorStream(true);
		pb.inheritIO();

		p = pb.start();
		p.waitFor();

		if (file.exists()) {
			System.out.println("Successfully Updated todo-app to " + snapshotVersion);
		}
		else {
			System.out.println("Failed to update todo-app to " + snapshotVersion);
		}
	}
}
