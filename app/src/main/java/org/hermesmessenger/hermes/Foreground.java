package org.hermesmessenger.hermes;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

class Foreground implements Application.ActivityLifecycleCallbacks {

    private static Foreground instance;

    public static void init(Application app){
        if (instance == null){
            instance = new Foreground();
            app.registerActivityLifecycleCallbacks(instance);
        }
    }

    public static Foreground get(){
        return instance;
    }

    private boolean foreground;

    public boolean isForeground(){
        return foreground;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        foreground = false;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        foreground = false;
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        foreground = false;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        foreground = true;
    }

    @Override
    public void onActivityStarted(Activity activity) {
        foreground = true;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        foreground = true;
    }


}