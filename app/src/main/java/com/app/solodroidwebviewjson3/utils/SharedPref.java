package com.app.solodroidwebviewjson3.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.app.solodroidwebviewjson3.models.Ads;
import com.app.solodroidwebviewjson3.models.Apps;
import com.app.solodroidwebviewjson3.models.Menu;
import com.app.solodroidwebviewjson3.models.MoreApps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharedPref {

    Context context;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private final String KEY_APPS = "apps";
    private final String KEY_MENU = "menus";
    private final String KEY_AD_NETWORKS = "ads";
    private final String KEY_MORE_APPS = "more_apps";

    public SharedPref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveAppsList(List<Apps> apps) {
        Gson gson = new Gson();
        String json = gson.toJson(apps);
        editor.putString(KEY_APPS, json);
        editor.apply();
    }

    public List<Apps> getAppsList() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(KEY_APPS, null);
        Type type = new TypeToken<ArrayList<Apps>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void saveMenuList(List<Menu> menus) {
        Gson gson = new Gson();
        String json = gson.toJson(menus);
        editor.putString(KEY_MENU, json);
        editor.apply();
    }

    public List<Menu> getMenuList() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(KEY_MENU, null);
        Type type = new TypeToken<ArrayList<Menu>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void saveAdList(List<Ads> ads) {
        Gson gson = new Gson();
        String json = gson.toJson(ads);
        editor.putString(KEY_AD_NETWORKS, json);
        editor.apply();
    }

    public List<Ads> getAdList() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(KEY_AD_NETWORKS, null);
        Type type = new TypeToken<ArrayList<Ads>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void saveMoreAppsList(List<MoreApps> moreApps) {
        Gson gson = new Gson();
        String json = gson.toJson(moreApps);
        editor.putString(KEY_MORE_APPS, json);
        editor.apply();
    }

    public List<MoreApps> getMoreAppsList() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(KEY_MORE_APPS, null);
        Type type = new TypeToken<ArrayList<MoreApps>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public Boolean getIsDarkTheme() {
        return sharedPreferences.getBoolean("theme", Constant.defaultDarkTheme);
    }

    public void setIsDarkTheme(Boolean isDarkTheme) {
        editor.putBoolean("theme", isDarkTheme);
        editor.apply();
    }

    public Integer getLastItemPosition() {
        return sharedPreferences.getInt("item_position", 0);
    }

    public void setLastItemPosition(int position) {
        editor.putInt("item_position", position);
        editor.apply();
    }

    public Integer getInAppReviewToken() {
        return sharedPreferences.getInt("in_app_review_token", 0);
    }

    public void updateInAppReviewToken(int value) {
        editor.putInt("in_app_review_token", value);
        editor.apply();
    }

}
