package ru.tinkoff.acquiring.sdk;


import android.os.Handler;
import android.os.Looper;

/**
 * @author Vitaliy Markus
 */
class AttachCardHandler extends Handler {

    public AttachCardHandler() {
        super(Looper.getMainLooper());
    }
}
