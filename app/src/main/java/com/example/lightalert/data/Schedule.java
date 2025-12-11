package com.example.lightalert.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

public class Schedule {
    private HashMap<String, HashMap<String, String>> scheduleData;

    public Schedule(JSONObject jsonSchedule) {
        scheduleData = new HashMap<>();
        if (jsonSchedule != null) {
            parseAndAddData(jsonSchedule);
        }
    }

    private void parseAndAddData(JSONObject jsonRoot) {
        try {
            JSONObject scheduleObj = jsonRoot.optJSONObject("schedule");
            if (scheduleObj == null) scheduleObj = jsonRoot;

            Iterator<String> daysIterator = scheduleObj.keys();
            while (daysIterator.hasNext()) {
                String day = daysIterator.next();
                JSONObject dayObject = scheduleObj.getJSONObject(day);

                updateDaySchedule(day, dayObject.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateDaySchedule(String dayName, String jsonString) {
        try {
            JSONObject dayData = new JSONObject(jsonString);
            HashMap<String, String> hoursMap = new HashMap<>();

            Iterator<String> keys = dayData.keys();
            while(keys.hasNext()) {
                String key = keys.next();
                String value = dayData.getString(key);
                try {
                    int hourInt = Integer.parseInt(key);
                    String adapterKey = String.format("%02d", hourInt - 1);
                    hoursMap.put(adapterKey, value);
                } catch (NumberFormatException e) {
                    hoursMap.put(key, value);
                }
            }
            scheduleData.put(dayName, hoursMap);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getStatus(String day, String hour) {
        HashMap<String, String> daySchedule = scheduleData.get(day);
        if (daySchedule != null) {
            return daySchedule.get(hour);
        }
        return null;
    }
}
