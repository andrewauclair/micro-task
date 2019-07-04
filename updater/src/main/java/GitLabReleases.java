import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class GitLabReleases {
	public static void main(String[] args) throws IOException {
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

		String currentReleaseName = "";
		String currentReleaseJar = "";

		System.out.println("Releases found on GitLab");
		System.out.println();

		Map<String, String> releases = new HashMap<>();

		for (int i = array.length() - 1; i >= 0; i--) {
			JSONObject obj = array.getJSONObject(i);

			String releaseName = obj.getString("name");
			String description = obj.getString("description");

			System.out.println(releaseName);

			String uploads = description.substring(description.indexOf("(/uploads/") + 1);

			String jar = "https://gitlab.com/mightymalakai33/todo-app" + uploads.substring(0, uploads.indexOf(')'));

			releases.put(releaseName, jar);

			if (i == 0) {
				currentReleaseName = releaseName;
				currentReleaseJar = jar;
			}
		}

		System.out.println();
		System.out.println("Enter release name to update to (leave empty to update to most recent release)");
		System.out.print("New Release >");

		Scanner scanner = new Scanner(System.in);
		String newRelease = scanner.nextLine();

		String releaseJar;
		if (newRelease.isEmpty()) {
			newRelease = currentReleaseName;
			releaseJar = currentReleaseJar;
		}
		else {
			releaseJar = releases.get(newRelease);
		}

		// download the jar file and rename it to todo-app.jar

		try (BufferedInputStream in = new BufferedInputStream(new URL(releaseJar).openStream());
			 FileOutputStream fileOutputStream = new FileOutputStream("todo-app.jar")) {
			byte[] dataBuffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
				fileOutputStream.write(dataBuffer, 0, bytesRead);
			}

			System.out.println("Updated to version '" + newRelease + "'");
		}
		catch (IOException e) {
			// handle exception
			e.printStackTrace();
		}
	}
}
