package org.hermesmessenger.hermes;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class Settings extends Application {

    private static String HermesUsername;
    private static String HermesUUID;


    public Settings(Context context) {

        SharedPreferences sharedPref = context.getSharedPreferences("preferences",0);
        HermesUUID = sharedPref.getString("UUID", "");
        HermesUsername = sharedPref.getString("username", "");
    }

    public static String getUsername() {
        return HermesUsername;
    }

    public static String getUUID() {
        return HermesUUID;
    }

}
