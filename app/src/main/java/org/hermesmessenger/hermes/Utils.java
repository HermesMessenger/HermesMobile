package org.hermesmessenger.hermes;

import android.app.Application;
import android.util.Log;

import java.io.Serializable;

public class Utils extends Application implements Serializable {

    @Override
    public void onCreate(){
        super.onCreate();
        Foreground.init(this);
    }

    public void setTimeout(Runnable runnable, int delay){
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            }
            catch (Exception err){
                Log.e("Timeout error", err.toString());
            }
        }).start();
    }

}
