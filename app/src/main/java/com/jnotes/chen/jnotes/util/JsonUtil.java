package com.jnotes.chen.jnotes.util;

import com.jnotes.chen.jnotes.jsonbean.Weather_data;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonUtil {
    public static String handleWeatherResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
             JSONArray sencondObject =  jsonObject.getJSONArray("lives");
             JSONObject data = sencondObject.getJSONObject(0);
             String weather = data.getString("weather");
             String temperature = data.getString("temperature");
             return weather+ " " + temperature +"°";
//            return weather;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
