package ru.tinkoff.acquiring.sample.ui;

import android.app.Application;

import ru.tinkoff.acquiring.sample.SessionParams;
import ru.tinkoff.acquiring.sdk.Journal;

/**
 * @author Vitaliy Markus
 */
public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Journal.setDebug(true);
        Journal.setDeveloperMode(SessionParams.IS_DEVELOPER_MODE);
    }
}
