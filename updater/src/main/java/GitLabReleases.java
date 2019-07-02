import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GitLabReleases {
	private static final String BASE_URL = "https://gitlab.com/api/v4/";

	private static List<String> getReleases() {

		return new ArrayList<>();
	}

	public static void main(String[] args) throws IOException {
		URL gitlabURL = new URL("https://gitlab.com/api/v4/projects/12882469/repository/tags/test/release");
		HttpsURLConnection connection = (HttpsURLConnection) gitlabURL.openConnection();

		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setRequestProperty("PRIVATE-TOKEN", "vRxU9YWpPK3sBSgG4Dmd");

		connection.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
		wr.writeBytes("{\"description\": \"Test\"}");
		wr.flush();
		wr.close();

		int responseCode = connection.getResponseCode();
		System.out.println("Response Code : " + responseCode);

		BufferedReader iny = new BufferedReader(
				new InputStreamReader(connection.getInputStream()));
		String output;
		StringBuffer response = new StringBuffer();

		while ((output = iny.readLine()) != null) {
			response.append(output);
		}
		iny.close();

		//printing result from response
		System.out.println(response.toString());
//		ProcessBuilder pb = new ProcessBuilder(
//				"curl",
////				"-s",
//				"-H",
//				"Content-Type: application/json",
//				"-H",
//				"PRIVATE-TOKEN: vRxU9YWpPK3sBSgG4Dmd",
//				"--data",
//				"{\"description\": \"Test\"}",
//
//				"--request",
//				"POST",
//				"https://gitlab.com/api/v4/projects/12882469/repository/tags/new-tag/release");

//		ProcessBuilder pb = new ProcessBuilder(
//				"curl",
////				"-s",
//				"-H",
//				"\"Content-Type: application/json\"",
//				"-H",
//				"\"PRIVATE-TOKEN: vRxU9YWpPK3sBSgG4Dmd\"",
////				"--data",
////				"\"{ \"name\": \"New release\", \"tag_name\": \"v0.3\", \"description\": \"Super nice release\", \"assets\": { \"links\": [{ \"name\": \"hoge\", \"url\": \"https://google.com\" }] } }\"",
//
//				"--request",
//				"POST",
//				"https://gitlab.com/api/v4/projects/12882469/releases");

		//{ "name": "New release", "tag_name": "v0.3", "description": "Super nice release", "assets": { "links": [{ "name": "hoge", "url": "https://google.com" }] } }
//		ProcessBuilder pb = new ProcessBuilder(
//				"curl",
//				"-s",
//				"-H",
//				"PRIVATE-TOKEN: vRxU9YWpPK3sBSgG4Dmd",
////				"-H",
////				"Content-Type: application/json",
////				"--request",
////				"POST",
////				"--data",
////				"{\"description\": \"Test\"}",
//				"https://gitlab.com/api/v4/projects/12882469/repository/tags");

//		pb.redirectErrorStream(true);
//		Process p = pb.start();
//		InputStream is = p.getInputStream();
//
//		Scanner scanner = new Scanner(is);
//
//		while (scanner.hasNextLine()) {
//			String line = scanner.nextLine();
//
//			System.out.println(line);
//		}
	}
}
