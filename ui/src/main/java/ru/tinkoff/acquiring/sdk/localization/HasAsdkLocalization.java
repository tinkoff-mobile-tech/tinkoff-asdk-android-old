package ru.tinkoff.acquiring.sdk.localization;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * @author a.shishkin1
 */
public interface HasAsdkLocalization {

    AsdkLocalization getAsdkLocalization();

    class AsdkLocalizationProperty implements HasAsdkLocalization {
        AsdkLocalization asdkLocalization;

        public AsdkLocalizationProperty(Context context) {
            asdkLocalization = AsdkLocalizations.unsafeGet(context);
        }

        @Override
        public AsdkLocalization getAsdkLocalization() {
            return asdkLocalization;
        }
    }
}



