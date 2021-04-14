package com.github.lobakov.componentcounter.restclient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportRetreiver {

    private static final String JSON_KEY = "HardwareModelName";
    private static final String CMDB_API = System.getenv("CMDB_API");
    private static final String REQUEST_TARGET = "/hardware-items?$select=HardwareModelName&$filter=InstalledInto eq ";
    private static final String REQUEST_URL = CMDB_API + REQUEST_TARGET;

    private HttpCookie cookie;

    public ReportRetreiver(HttpCookie cookie) {
        this.cookie = cookie;
    }

    public Map<String, Integer> getReport(List<String> sapIds) throws IOException, URISyntaxException {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (String sap : sapIds) {
            Map<String, Integer> report = getHardwareReportBySapId(sap);
            report.forEach((key, value) -> result.merge(key, value, (oldVal, newVal) -> (oldVal + newVal)));
        }
        return result;
    }

    private Map<String, Integer> getHardwareReportBySapId(String sap) throws IOException, URISyntaxException {
        String url = REQUEST_URL + "\'" + sap + "\'";
        HttpURLConnection connection = getConnection(url);
        String response = getHardwareReport(connection);
        return responseToMap(response);
    }

    private HttpURLConnection getConnection(String uri) throws IOException, URISyntaxException {
        URL url = new URL(uri);
        URI encoded = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(),
                url.getPath(), url.getQuery(), url.getRef());

        HttpURLConnection connection = (HttpURLConnection) encoded.toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Cookie", cookie.getName() + "=" + cookie.getValue());
        connection.setDoOutput(true);
        return connection;
    }

    private String getHardwareReport(HttpURLConnection connection) throws IOException {
        connection.connect();
        StringBuilder response = new StringBuilder();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        return response.toString();
    }

    private Map<String, Integer> responseToMap(String response) {
        JSONArray jsonArray = new JSONArray(response);
        Map<String, Integer> result = new HashMap<>();
        String authName = "";
        for (Object jsonObject: jsonArray) {
            authName = ((JSONObject) jsonObject).getString(JSON_KEY);
            if (!result.containsKey(authName)) {
                result.put(authName, 1);
            } else {
                result.put(authName, result.get(authName) + 1);
            }
        }
        return result;
    }
}
