package com.github.lobakov.componentcounter.restclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.net.URL;
import java.net.HttpURLConnection;
import org.json.JSONObject;

public class Authenticator {

    private static final String AUTH_URL = System.getenv("CMDB_LOGIN");
    private static final int LOGIN_INDEX = 0;
    private static final int PASS_INDEX = 1;
    private static final int OTP_INDEX = 2;

    private int httpResponseCode;
    private HttpCookie cookie;

    public HttpCookie authenticate(String[] credentials) throws IOException {
        HttpURLConnection connection = getConnection(AUTH_URL);

        String request = String.format("{\"Name\": \"%s\", \"DomainPassword\": \"%s\", \"OtpPassword\": \"%s\"}",
                credentials[LOGIN_INDEX], credentials[PASS_INDEX], credentials[OTP_INDEX]);

        httpResponseCode = sendAuthRequest(connection, request);
        String response = getAuthResponse(connection);
        this.cookie = responseToCookie(response);
        return cookie;
    }

    public int getHttpResponseCode() {
        return this.httpResponseCode;
    }

    public HttpCookie getCookie() {
        return cookie;
    }

    private HttpURLConnection getConnection(String uri) throws IOException {
        URL url = new URL(uri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        return connection;
    }

    private int sendAuthRequest(HttpURLConnection connection, String request) throws IOException {
        try(OutputStream os = connection.getOutputStream()) {
            byte[] input = request.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        return connection.getResponseCode();
    }

    private String getAuthResponse(HttpURLConnection connection) throws IOException {
        StringBuilder response = new StringBuilder();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        return response.toString();
    }

    private HttpCookie responseToCookie(String response) {
        JSONObject jsonObject = new JSONObject(response);
        String authName = jsonObject.getString("Name");
        String authValue = jsonObject.getString("Value");
        HttpCookie cookie = new HttpCookie(authName, authValue);
        return cookie;
    }
}
