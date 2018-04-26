package ru.tinkoff.acquiring.sdk;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Vitaliy Markus
 */
public class AttachCardFormHandler extends Handler {

    static AttachCardFormHandler INSTANCE = new AttachCardFormHandler();

    public static final int CARD_ID = 0;
    public static final int SHOW_LOOP_CONFIRMATIONS = 1;

    public AttachCardFormHandler() {
        super(Looper.getMainLooper());
    }

    private Set<IAttachCardFormActivity> callbacks = new HashSet<>();

    public void register(IAttachCardFormActivity activity) {
        callbacks.add(activity);
    }

    public void unregister(IAttachCardFormActivity activity) {
        callbacks.remove(activity);
    }

    @Override
    public void handleMessage(Message msg) {
        int action = msg.what;

        switch (action) {
            case CARD_ID:
                for (IAttachCardFormActivity activity : callbacks) {
                    activity.onAttachCardId((String) msg.obj);
                }
                return;
            case SHOW_LOOP_CONFIRMATIONS:
                for (IAttachCardFormActivity activity : callbacks) {
                    activity.showLoopConfirmations((String)msg.obj);
                }
                return;
        }

        super.handleMessage(msg);
    }
}
