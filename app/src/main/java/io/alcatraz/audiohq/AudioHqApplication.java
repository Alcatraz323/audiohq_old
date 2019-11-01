package io.alcatraz.audiohq;

import android.app.Application;
import android.content.Context;


public class AudioHqApplication extends Application {
    private Context ctx;
    //TODO : Check all ".equals()" to apply PropertyManager
    //TODO : Set Empty View for all adapter views
    @Override
    public void onCreate() {
        ctx = getApplicationContext();
        super.onCreate();
    }

    public Context getOverallContext() {
        return ctx;
    }

}
