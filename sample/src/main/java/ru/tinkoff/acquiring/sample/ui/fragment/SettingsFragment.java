package ru.tinkoff.acquiring.sample.ui.fragment;


import android.os.Bundle;
import android.preference.PreferenceFragment;

import ru.tinkoff.acquiring.sample.R;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}
