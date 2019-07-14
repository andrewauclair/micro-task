// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.os;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GitLabReleases {
	private static List<String> getVersions() throws IOException {
		// curl --header "PRIVATE-TOKEN: gDybLx3yrUK_HLp3qPjS" "http://localhost:3000/api/v4/projects/24/releases"

		URL gitlabURL = new URL("https://gitlab.com/api/v4/projects/12882469/releases");
		HttpsURLConnection connection = (HttpsURLConnection) gitlabURL.openConnection();

		connection.setRequestProperty("PRIVATE-TOKEN", "jMKLMkAQ2WfaWz43zNVz");

		connection.setDoOutput(true);

		BufferedReader iny = new BufferedReader(
				new InputStreamReader(connection.getInputStream()));
		String output;
		StringBuilder response = new StringBuilder();

		while ((output = iny.readLine()) != null) {
			response.append(output);
		}
		iny.close();

		String jsonStr = response.toString();

		JSONArray array = new JSONArray(jsonStr);

		List<String> releaseNames = new ArrayList<>();

		for (int i = array.length() - 1; i >= 0; i--) {
			JSONObject obj = array.getJSONObject(i);

			String releaseName = obj.getString("name");

			releaseNames.add(releaseName);
		}

		return releaseNames;
	}

	public static void printReleases() throws IOException {
		List<String> releases = getVersions();

		System.out.println("Releases found on GitLab");
		System.out.println();

		for (String release : releases) {
			System.out.println(release);
		}

		System.out.println();
	}

	public static void updateToRelease(String release) throws IOException {
		// curl --header "PRIVATE-TOKEN: gDybLx3yrUK_HLp3qPjS" "http://localhost:3000/api/v4/projects/24/releases"

		URL gitlabURL = new URL("https://gitlab.com/api/v4/projects/12882469/releases");
		HttpsURLConnection connection = (HttpsURLConnection) gitlabURL.openConnection();

		connection.setRequestProperty("PRIVATE-TOKEN", "jMKLMkAQ2WfaWz43zNVz");

		connection.setDoOutput(true);

		BufferedReader iny = new BufferedReader(
				new InputStreamReader(connection.getInputStream()));
		String output;
		StringBuilder response = new StringBuilder();

		while ((output = iny.readLine()) != null) {
			response.append(output);
		}
		iny.close();

		String jsonStr = response.toString();

		JSONArray array = new JSONArray(jsonStr);

		for (int i = 0; i < array.length() - 1; i++) {
			JSONObject obj = array.getJSONObject(i);

			String releaseName = obj.getString("name");
			String description = obj.getString("description");

			String uploads = description.substring(description.indexOf("(/uploads/") + 1);

			String jar = "https://gitlab.com/mightymalakai33/todo-app" + uploads.substring(0, uploads.indexOf(')'));

			if (release.isEmpty() || releaseName.equals(release)) {
				System.out.println("Downloading: " + releaseName);

				// download the jar file and rename it to todo-app.jar

				try (BufferedInputStream in = new BufferedInputStream(new URL(jar).openStream());
					 FileOutputStream fileOutputStream = new FileOutputStream("todo-app.jar")) {
					byte[] dataBuffer = new byte[1024];
					int bytesRead;
					while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
						fileOutputStream.write(dataBuffer, 0, bytesRead);
					}

					System.out.println("Updated to version '" + releaseName + "'");
				}
				catch (IOException e) {
					// handle exception
					e.printStackTrace();
				}

				break;
			}
		}
	}
}
