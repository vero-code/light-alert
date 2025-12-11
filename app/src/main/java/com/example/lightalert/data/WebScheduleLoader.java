package com.example.lightalert.data;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Iterator;

public class WebScheduleLoader {

    private static final String TARGET_URL = "";
    private static final String REFERER_URL = "";
    private static final String GROUP_NAME = "";

    public interface ScheduleCallback {
        void onLoaded(String todayData, String tomorrowData);
        void onError(String error);
    }

    public static void loadSchedule(ScheduleCallback callback) {
        new Thread(() -> {
            try {
                Document doc = Jsoup.connect(TARGET_URL)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                        .header("Accept-Language", "uk-UA,uk;q=0.9,en-US;q=0.8,en;q=0.7")
                        .header("Referer", REFERER_URL)
                        .timeout(15000)
                        .get();

                Elements scripts = doc.select("script");
                String foundScriptContent = null;

                for (Element script : scripts) {
                    if (script.html().contains("DisconSchedule.fact")) {
                        foundScriptContent = script.html();
                        break;
                    }
                }

                if (foundScriptContent == null) {
                    callback.onError("Script not found on site.");
                    return;
                }

                String jsonString = null;
                String marker = "DisconSchedule.fact";
                int markerIndex = foundScriptContent.indexOf(marker);

                if (markerIndex != -1) {
                    int openBraceIndex = foundScriptContent.indexOf("{", markerIndex);
                    if (openBraceIndex != -1) {
                        int braceCount = 1;
                        int i = openBraceIndex + 1;
                        StringBuilder sb = new StringBuilder();
                        sb.append("{");

                        while (braceCount > 0 && i < foundScriptContent.length()) {
                            char c = foundScriptContent.charAt(i);
                            sb.append(c);

                            if (c == '{') {
                                braceCount++;
                            } else if (c == '}') {
                                braceCount--;
                            }
                            i++;
                        }

                        if (braceCount == 0) {
                            jsonString = sb.toString();
                        }
                    }
                }

                if (jsonString != null) {
                    JSONObject jsonData = new JSONObject(jsonString);
                    JSONObject dataObj = jsonData.getJSONObject("data");

                    String todayKeyStr = String.valueOf(jsonData.optLong("today"));

                    String todaySchedule = null;
                    String tomorrowSchedule = null;

                    Iterator<String> keys = dataObj.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        JSONObject dayData = dataObj.getJSONObject(key);

                        if (dayData.has(GROUP_NAME)) {
                            String rawSchedule = dayData.getJSONObject(GROUP_NAME).toString();

                            if (key.equals(todayKeyStr)) {
                                todaySchedule = rawSchedule;
                            } else {
                                tomorrowSchedule = rawSchedule;
                            }
                        }
                    }

                    if (todaySchedule == null && dataObj.length() > 0) {
                        Iterator<String> keyIter = dataObj.keys();
                        if (keyIter.hasNext()) {
                            String k1 = keyIter.next();
                            todaySchedule = dataObj.getJSONObject(k1).optString(GROUP_NAME);
                            if (keyIter.hasNext()) {
                                String k2 = keyIter.next();
                                tomorrowSchedule = dataObj.getJSONObject(k2).optString(GROUP_NAME);
                            }
                        }
                    }

                    callback.onLoaded(todaySchedule, tomorrowSchedule);
                } else {
                    callback.onError("Failed to extract JSON.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                callback.onError("Parsing error: " + e.getMessage());
            }
        }).start();
    }
}