package org.hermesmessenger.hermes;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class Settings extends Application {

    private String HermesUsername;
    private String HermesUUID;


    public Settings(Context context) {

        SharedPreferences sharedPref = context.getSharedPreferences("preferences",0);
        HermesUUID = sharedPref.getString("UUID", "");
        HermesUsername = sharedPref.getString("Username", "");
    }

    public String getUsername() {
        return HermesUsername;
    }

    public String getUUID() {
        return HermesUUID;
    }

}
