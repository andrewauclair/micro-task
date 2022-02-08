// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.os;

import com.andrewauclair.microtask.ConsoleTable;
import com.andrewauclair.microtask.TaskException;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.*;
import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

@SuppressWarnings("CanBeFinal")
public class GitLabReleases {
	private static final int PROJECT_ID = 12882469;
	private static final String PROJECT_URL = "https://gitlab.com/api/v4/projects/" + PROJECT_ID + "/";
	// TODO Is there a way that we can request a token or not even use one here? My pipelines and releases should be open to the guest level
	private static final String PRIVATE_TOKEN = "jMKLMkAQ2WfaWz43zNVz";

	private static final Map<BigInteger, X509Certificate> trustedCertificates = new HashMap<>();

	public GitLabReleases() throws Exception {
		TrustManagerFactory tmf = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());

		// Using null here initialises the TMF with the default trust store.
		tmf.init((KeyStore) null);

		// Get hold of the default trust manager
		X509TrustManager x509Tm = null;
		for (TrustManager tm : tmf.getTrustManagers()) {
			if (tm instanceof X509TrustManager) {
				x509Tm = (X509TrustManager) tm;
				break;
			}
		}

		// Wrap it in your own class.
		final X509TrustManager finalTm = x509Tm;

		Objects.requireNonNull(finalTm);

		X509TrustManager customTm = new X509TrustManager() {
			@Override
			public void checkClientTrusted(X509Certificate[] chain,
			                               String authType) throws CertificateException {
				finalTm.checkClientTrusted(chain, authType);
			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain,
			                               String authType) throws CertificateException {
				try {
					finalTm.checkServerTrusted(chain, authType);
				}
				catch (CertificateException e) {
					X509Certificate cert = chain[chain.length - 1];

					if (trustedCertificates.get(cert.getSerialNumber()) != null) {
						// we've already trusted this certificate during this run
						return;
					}

					System.out.println(cert.getPublicKey().toString());
					System.out.println(cert.getIssuerX500Principal().toString());
					System.out.println(cert.getIssuerDN().toString());
					System.out.println(cert.getSigAlgName());
					System.out.println(cert.getType());
					System.out.println();
					System.out.print("Do you trust this certificate? [Y/n] ");

					try {
						char ch = (char) System.in.read();

						System.out.println(ch);

						if (ch == 'n' || ch == 'N') {
							throw e;
						}
						else {
							// trust the certificate for the rest of the run
							trustedCertificates.put(cert.getSerialNumber(), cert);
						}
					}
					catch (IOException e1) {
						e.printStackTrace();
					}
				}
			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return finalTm.getAcceptedIssuers();
			}
		};

		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, new TrustManager[] {customTm}, null);

		// You don't have to set this as the default context,
		// it depends on the library you're using.
		SSLContext.setDefault(sslContext);
	}

	public static class ReleasePipeline {
		public final String versionName;
		public final int pipelineID;

		public ReleasePipeline(String versionName, int pipelineID) {
			this.versionName = versionName;
			this.pipelineID = pipelineID;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			final ReleasePipeline pipeline = (ReleasePipeline) o;
			return pipelineID == pipeline.pipelineID &&
					Objects.equals(versionName, pipeline.versionName);
		}

		@Override
		public int hashCode() {
			return Objects.hash(versionName, pipelineID);
		}

		@Override
		public String toString() {
			return "ReleasePipeline{" +
					"versionName='" + versionName + '\'' +
					", pipelineID=" + pipelineID +
					'}';
		}
	}

	public List<ReleasePipeline> getReleases(Proxy proxy, boolean includeSnapshots) throws IOException {
		String pipelinePath = "pipelines?scope=finished&status=success";

		if (!includeSnapshots) {
			pipelinePath += "&scope=tags";
		}
		else {
			pipelinePath += "&scope=branches";
		}

		JSONArray pipelineJSON = getJSONArray(pipelinePath, proxy);
		List<ReleasePipeline> releases = new ArrayList<>();

		for (int i = 0; i < pipelineJSON.length(); i++) {
			JSONObject obj = pipelineJSON.getJSONObject(i);

			String name = obj.getString("ref");

			if (releases.stream().noneMatch(releasePipeline -> releasePipeline.versionName.equals(name))) {
				releases.add(new ReleasePipeline(name, obj.getInt("id")));
			}
		}

		return releases;
	}

	public boolean updateToRelease(ReleasePipeline release, Proxy proxy) throws IOException {
		URL artifactURL = getArtifactURL(release, proxy);

		String zip = release.versionName + ".zip";

		if (downloadFile(proxy, artifactURL, zip)) {
			extractFolder(zip, ".");

			Files.delete(new File(zip).toPath());

			// now that we downloaded the new one without issues we can replace it
			Files.move(new File("build/libs/micro-task.jar").toPath(), new File("micro-task.jar").toPath(), StandardCopyOption.REPLACE_EXISTING);

			Files.delete(new File("build/libs").toPath());
			Files.delete(new File("build").toPath());

			return true;
		}

		return false;
	}

	private URL getArtifactURL(ReleasePipeline release, Proxy proxy) throws IOException {
		String artifactsBaseURL = "https://gitlab.com/api/v4/projects/12882469/jobs/";
		JSONArray jobsJSON = getJSONArray("pipelines/" + release.pipelineID + "/jobs", proxy);

		for (int i = 0; i < jobsJSON.length(); i++) {
			JSONObject jsonObject = jobsJSON.getJSONObject(i);

			if (!jsonObject.isNull("artifacts_file")) {
				return new URL(artifactsBaseURL + jsonObject.getInt("id") + "/artifacts");
			}
		}
		throw new TaskException("No artifacts found for release '" + release.versionName + "'");
	}

	private boolean downloadFile(Proxy proxy, URL URL, String fileName) throws IOException {
		HttpsURLConnection connection = (HttpsURLConnection) URL.openConnection(proxy);
		connection.setRequestProperty("PRIVATE-TOKEN", "jMKLMkAQ2WfaWz43zNVz");

		try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
		     FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
			fileOutputStream.write(in.readAllBytes());

			return true;
		}
		catch (IOException e) {
			// handle exception
			e.printStackTrace();
		}
		return false;
	}

	public String changelogForRelease(ReleasePipeline release, Proxy proxy, boolean snapshot) throws IOException {
		if (snapshot) {
			JSONArray jobsJSON = getJSONArray("pipelines/" + release.pipelineID + "/jobs", proxy);

			for (int i = 0; i < jobsJSON.length(); i++) {
				JSONObject jsonObject = jobsJSON.getJSONObject(i);

				if (!jsonObject.isNull("commit")) {
					return jsonObject.getJSONObject("commit").getString("message");
				}
			}
			return "";
		}
		else {
			JSONObject releaseJSON = getJSONObject("releases/" + release.versionName, proxy);

			return releaseJSON.getString("description");
		}
	}

	public static JSONArray getJSONArray(String path, Proxy proxy) throws IOException {
		URL gitlabURL = new URL(PROJECT_URL + path);

		HttpsURLConnection connection = (HttpsURLConnection) gitlabURL.openConnection(proxy);

		connection.setRequestProperty("PRIVATE-TOKEN", PRIVATE_TOKEN);

		connection.setDoOutput(true);

		return readJSONArrayFromConnection(connection);
	}

	public static JSONObject getJSONObject(String path, Proxy proxy) throws IOException {
		URL gitlabURL = new URL(PROJECT_URL + path);

		HttpsURLConnection connection = (HttpsURLConnection) gitlabURL.openConnection(proxy);

		connection.setRequestProperty("PRIVATE-TOKEN", PRIVATE_TOKEN);

		connection.setDoOutput(true);

		return readJSONObjectFromConnection(connection);
	}

	private static JSONArray readJSONArrayFromConnection(HttpsURLConnection connection) throws IOException {
		return new JSONArray(readFromConnection(connection));
	}

	private static JSONObject readJSONObjectFromConnection(HttpsURLConnection connection) throws IOException {
		return new JSONObject(readFromConnection(connection));
	}

	private static String readFromConnection(HttpsURLConnection connection) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
			StringBuilder builder = new StringBuilder();

			reader.lines().forEach(builder::append);

			return builder.toString();
		}
	}

	public void unzip(String zipFilePath, String file, String dest) throws IOException {
		ZipFile zipFile = new ZipFile(zipFilePath);

		Enumeration zipFileEntries = zipFile.entries();

		// Process each entry
		while (zipFileEntries.hasMoreElements()) {
			// grab a zip file entry
			ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();

			if (entry.getName().equals(file)) {
				System.out.println("Unzip " + new File(entry.getName()).getAbsolutePath() + " to " + new File(file).getAbsolutePath());
				BufferedInputStream is = new BufferedInputStream(zipFile.getInputStream(entry));
				extractFile(is, dest);
				is.close();
			}
		}

		zipFile.close();
	}

	private void extractFile(BufferedInputStream zipIn, String filePath) throws IOException {
		byte[] buffer = new byte[1024];
		int count;

		FileOutputStream fos = new FileOutputStream(filePath);
		while ((count = zipIn.read(buffer)) > 0) {
			fos.write(buffer, 0, count);
		}
		fos.close();
	}

	private static void extractFolder(String zipFile,String extractFolder)
	{
		try (ZipFile zip = new ZipFile(zipFile))
		{
			int BUFFER = 2048;
//			File file = new File(zipFile);
//
//			ZipFile zip = new ZipFile(file);
			String newPath = extractFolder;

			new File(newPath).mkdir();
			Enumeration zipFileEntries = zip.entries();

			// Process each entry
			while (zipFileEntries.hasMoreElements())
			{
				// grab a zip file entry
				ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
				String currentEntry = entry.getName();

				File destFile = new File(newPath, currentEntry);
				//destFile = new File(newPath, destFile.getName());
				File destinationParent = destFile.getParentFile();

				// create the parent directory structure if needed
				destinationParent.mkdirs();

				if (!entry.isDirectory())
				{
					BufferedInputStream is = new BufferedInputStream(zip
							.getInputStream(entry));
					int currentByte;
					// establish buffer for writing file
					byte data[] = new byte[BUFFER];

					// write the current file to disk
					FileOutputStream fos = new FileOutputStream(destFile);
					BufferedOutputStream dest = new BufferedOutputStream(fos,
							BUFFER);

					// read and write until last byte is encountered
					while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
						dest.write(data, 0, currentByte);
					}
					dest.flush();
					dest.close();
					is.close();
				}


			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
//			Log("ERROR: "+e.getMessage());
		}

	}
}
