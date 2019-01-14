package com.jnotes.chen.jnotes.util;

import com.google.gson.Gson;
import com.jnotes.chen.jnotes.jsonbean.JsonRootBean;

public class JsonUtil {
    public static JsonRootBean handleWeatherResponse(String response) {
        try {
            return new Gson().fromJson(response, JsonRootBean.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
