package org.hermesmessenger.hermes;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class Settings extends Application {

    public static String HermesURL = "https://hermesmessenger-testing.duckdns.org";
    public static String HermesUsername;
    public static String HermesUUID;

    public Settings(Context context) {

        SharedPreferences sharedPref = context.getSharedPreferences("preferences",0);
        HermesUUID = sharedPref.getString("UUID", "");
        HermesUsername = sharedPref.getString("Username", "");
    }

}
