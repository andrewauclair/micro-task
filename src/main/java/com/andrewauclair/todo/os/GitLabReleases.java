// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.os;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.*;
import java.io.*;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class GitLabReleases {
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
		sslContext.init(null, new TrustManager[]{customTm}, null);

// You don't have to set this as the default context,
// it depends on the library you're using.
		SSLContext.setDefault(sslContext);
	}
	
	public List<String> getVersions() throws IOException {
		JSONArray array = getReleasesJSON();
		
		List<String> releaseNames = new ArrayList<>();
		
		for (int i = array.length() - 1; i >= 0; i--) {
			JSONObject obj = array.getJSONObject(i);
			
			String releaseName = obj.getString("name");
			
			releaseNames.add(releaseName);
		}
		
		return releaseNames;
	}
	
	private JSONArray getReleasesJSON() throws IOException {
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
		
		return new JSONArray(jsonStr);
	}
	
	public boolean updateToRelease(String release) throws IOException {
		JSONArray array = getReleasesJSON();
		
		for (int i = 0; i < array.length() - 1; i++) {
			JSONObject obj = array.getJSONObject(i);
			
			String releaseName = obj.getString("name");
			String description = obj.getString("description");
			
			String uploads = description.substring(description.indexOf("(/uploads/") + 1);
			
			String jar = "https://gitlab.com/mightymalakai33/todo-app" + uploads.substring(0, uploads.indexOf(')'));
			
			if (release.isEmpty() || releaseName.equals(release)) {
				// download the jar file and rename it to todo-app.jar
				
				try (BufferedInputStream in = new BufferedInputStream(new URL(jar).openStream());
					 FileOutputStream fileOutputStream = new FileOutputStream("todo-app.jar")) {
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
				
				break;
			}
		}
		
		return false;
	}
}
