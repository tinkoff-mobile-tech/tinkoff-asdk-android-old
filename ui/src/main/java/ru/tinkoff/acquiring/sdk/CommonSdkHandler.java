package ru.tinkoff.acquiring.sdk;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Vitaliy Markus
 */
public class CommonSdkHandler extends Handler {

    static CommonSdkHandler INSTANCE = new CommonSdkHandler();

    public static final int SUCCESS = 0;
    public static final int CANCEL = 1;
    public static final int EXCEPTION = 2;
    public static final int START_3DS = 3;
    public static final int SHOW_ERROR_DIALOG = 4;
    public static final int NO_NETWORK = 5;

    public CommonSdkHandler() {
        super(Looper.getMainLooper());
    }

    private Set<IBaseSdkActivity> callbacks = new HashSet<>();

    public void register(IBaseSdkActivity activity) {
        callbacks.add(activity);
    }

    public void unregister(IBaseSdkActivity activity) {
        callbacks.remove(activity);
    }

    @Override
    public void handleMessage(Message msg) {
        int action = msg.what;

        switch (action) {
            case SUCCESS:
                for (IBaseSdkActivity activity : callbacks) {
                    activity.success();
                }
                return;
            case CANCEL:
                for (IBaseSdkActivity activity : callbacks) {
                    activity.cancel();
                }
                return;
            case EXCEPTION:
                for (IBaseSdkActivity activity : callbacks) {
                    activity.exception((Exception) msg.obj);
                }
                return;
            case START_3DS:
                for (IBaseSdkActivity activity : callbacks) {
                    activity.start3DS((ThreeDsData) msg.obj);
                }
                return;
            case SHOW_ERROR_DIALOG:
                for (IBaseSdkActivity activity : callbacks) {
                    activity.showErrorDialog((Exception) msg.obj);
                }
                return;
            case NO_NETWORK:
                for (IBaseSdkActivity activity : callbacks) {
                    activity.noNetwork();
                }
                return;
        }

        super.handleMessage(msg);
    }
}
