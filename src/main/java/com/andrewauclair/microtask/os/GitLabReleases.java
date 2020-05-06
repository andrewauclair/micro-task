// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.os;

import com.andrewauclair.microtask.ConsoleTable;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.*;
import java.io.*;
import java.math.BigInteger;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@SuppressWarnings("CanBeFinal")
public class GitLabReleases {
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

	public List<String> getVersions(Proxy proxy) throws IOException {
		JSONArray array = getReleasesJSON(proxy);

		List<String> releaseNames = new ArrayList<>();

		for (int i = array.length() - 1; i >= 0; i--) {
			JSONObject obj = array.getJSONObject(i);

			String releaseName = obj.getString("name");

			releaseNames.add(releaseName);
		}

		return releaseNames;
	}

	private JSONArray getReleasesJSON(Proxy proxy) throws IOException {
		// curl --header "PRIVATE-TOKEN: gDybLx3yrUK_HLp3qPjS" "http://localhost:3000/api/v4/projects/24/releases"

		URL gitlabURL = new URL("https://gitlab.com/api/v4/projects/12882469/releases");
		HttpsURLConnection connection = (HttpsURLConnection) gitlabURL.openConnection(proxy);

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

		return new JSONArray(jsonStr);
	}

	public boolean updateToRelease(String release, Proxy proxy) throws IOException {
		JSONArray array = getReleasesJSON(proxy);

		for (int i = 0; i < array.length() - 1; i++) {
			JSONObject obj = array.getJSONObject(i);

			String releaseName = obj.getString("name");
			String description = obj.getString("description");

			String uploads = description.substring(description.indexOf("(/uploads/") + 1);

			String jar = "https://gitlab.com/mightymalakai33/micro-task" + uploads.substring(0, uploads.indexOf(')'));

			if (release.isEmpty() || releaseName.equals(release)) {
				// download the jar file and rename it to micro-task.jar

				if (downloadFile(proxy, jar, "micro-task.jar")) {
					return true;
				}

				break;
			}
		}

		return false;
	}

	private boolean downloadFile(Proxy proxy, String URL, String fileName) throws IOException {
		URL gitlabURL = new URL(URL);
		HttpsURLConnection connection = (HttpsURLConnection) gitlabURL.openConnection(proxy);
		connection.setRequestProperty("PRIVATE-TOKEN", "jMKLMkAQ2WfaWz43zNVz");

		try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
		     FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
			byte[] dataBuffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
				fileOutputStream.write(dataBuffer, 0, bytesRead);
			}

			return true;
		}
		catch (IOException e) {
			// handle exception
			e.printStackTrace();
		}
		return false;
	}

	public String changelogForRelease(String release, Proxy proxy) throws IOException {
		JSONArray array = getReleasesJSON(proxy);

		for (int i = 0; i < array.length() - 1; i++) {
			JSONObject obj = array.getJSONObject(i);

			String releaseName = obj.getString("name");
			String description = obj.getString("description");

			String uploads = description.substring(description.indexOf("(/uploads/") + 1);

			String jar = "https://gitlab.com/mightymalakai33/micro-task" + uploads.substring(0, uploads.indexOf(')'));

			if (release.isEmpty() || releaseName.equals(release)) {
				return description.substring(description.indexOf("Changelog"), description.indexOf("Download this release"));
			}
		}
		return "";
	}

	public void listSnapshotVersions(OSInterface osInterface, Proxy proxy) throws IOException, ParseException {
		// curl --header "PRIVATE-TOKEN: gDybLx3yrUK_HLp3qPjS" "https://gitlab.com/api/v4/projects/12882469/jobs?scope=success"

		JSONArray jsonArray = getPipelineJSON(proxy);

//		Map<In/teger, String> refs = new HashMap<>();
		List<String> refs = new ArrayList<>();

		TimeZone tz = TimeZone.getTimeZone("UTC");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'");
		dateFormat.setTimeZone(tz);

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

		ConsoleTable table = new ConsoleTable(osInterface);
		table.setHeaders("Branch", "Date", "Expires", "Description");
		table.enableAlternatingColors();
		table.enableWordWrap();

		for (int i = 0; i < jsonArray.length() - 1; i++) {
			JSONObject obj = jsonArray.getJSONObject(i);

			int id = obj.getInt("id");

			String ref = obj.getString("ref");

			boolean tag = obj.getBoolean("tag");

			String title = obj.getJSONObject("commit").getString("title");

			String finished = obj.getString("finished_at");

			Date finishedDate = dateFormat.parse(finished);

//			System.out.println(String.format("%d %s %s %s", id, ref, title, obj.get("artifacts_expire_at")));

			if (!obj.isNull("artifacts_expire_at") && !tag && !refs.contains(ref)) {
				refs.add(ref);

				String expires = obj.getString("artifacts_expire_at");
				Date expireDate = dateFormat.parse(expires);


				String finishedFormat = finishedDate.toInstant().atZone(osInterface.getZoneId()).format(dateTimeFormatter);
				String expiresFormat = expireDate.toInstant().atZone(osInterface.getZoneId()).format(dateTimeFormatter);

				table.addRow(ref, finishedFormat, expiresFormat, title);
			}
		}

		table.print();
	}

	private JSONArray getPipelineJSON(Proxy proxy) throws IOException {
		URL gitlabURL = new URL("https://gitlab.com/api/v4/projects/12882469/jobs?scope=success");
		HttpsURLConnection connection = (HttpsURLConnection) gitlabURL.openConnection(proxy);

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

		return new JSONArray(jsonStr);
	}

	public boolean updateToSnapshotRelease(String name, Proxy proxy) throws IOException {
		// GET /projects/:id/jobs/:job_id/artifacts
		String artifactsBaseURL = "https://gitlab.com/api/v4/projects/12882469/jobs/";

		JSONArray jsonArray = getPipelineJSON(proxy);

		List<String> refs = new ArrayList<>();

		TimeZone tz = TimeZone.getTimeZone("UTC");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'");
		dateFormat.setTimeZone(tz);

		for (int i = 0; i < jsonArray.length() - 1; i++) {
			JSONObject obj = jsonArray.getJSONObject(i);

			int id = obj.getInt("id");

			String ref = obj.getString("ref");

			boolean tag = obj.getBoolean("tag");

			if (!obj.isNull("artifacts_expire_at") && !tag && !refs.contains(ref)) {
				refs.add(ref);

				if (ref.equals(name)) {
					// update to this snapshot version
					if (downloadFile(proxy, artifactsBaseURL + id + "/artifacts", ref + ".zip")) {
						unzip(ref + ".zip");

						Files.delete(new File(ref + ".zip").toPath());
					}
				}
			}
		}

		return true;
	}

	public String messageForSnapshot(String version, Proxy proxy) throws IOException {
// GET /projects/:id/jobs/:job_id/artifacts
		String artifactsBaseURL = "https://gitlab.com/api/v4/projects/12882469/jobs/";

		JSONArray jsonArray = getPipelineJSON(proxy);

		List<String> refs = new ArrayList<>();

		TimeZone tz = TimeZone.getTimeZone("UTC");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'");
		dateFormat.setTimeZone(tz);

		for (int i = 0; i < jsonArray.length() - 1; i++) {
			JSONObject obj = jsonArray.getJSONObject(i);

			int id = obj.getInt("id");

			String ref = obj.getString("ref");

			boolean tag = obj.getBoolean("tag");

			if (!obj.isNull("artifacts_expire_at") && !tag && !refs.contains(ref)) {
				refs.add(ref);

				if (ref.equals(version)) {
					return obj.getJSONObject("commit").getString("message");
				}
			}
		}
		return "";
	}

	/**
	 * Size of the buffer to read/write data
	 */
	private static final int BUFFER_SIZE = 4096;

	/**
	 * Extracts a zip file specified by the zipFilePath to a directory specified by
	 * destDirectory (will be created if does not exists)
	 *
	 * @param zipFilePath
	 * @throws IOException
	 */
	public void unzip(String zipFilePath) throws IOException {
		ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
		ZipEntry entry = zipIn.getNextEntry();
		// iterates over entries in the zip file
		while (entry != null) {
			String filePath = entry.getName();
			if (!entry.isDirectory() && filePath.equals("build/libs/micro-task.jar")) {
				// if the entry is a file, extracts it
				extractFile(zipIn, "micro-task.jar");
			}
			zipIn.closeEntry();
			entry = zipIn.getNextEntry();
		}
		zipIn.close();
	}

	/**
	 * Extracts a zip entry (file entry)
	 *
	 * @param zipIn
	 * @param filePath
	 * @throws IOException
	 */
	private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
		byte[] bytesIn = new byte[BUFFER_SIZE];
		int read = 0;
		while ((read = zipIn.read(bytesIn)) != -1) {
			bos.write(bytesIn, 0, read);
		}
		bos.close();
	}
}
