/*
 * Copyright © 2016 Tinkoff Bank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.tinkoff.acquiring.sdk;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.HashSet;
import java.util.Set;

/**
 * @author a.shishkin1
 */
class SdkHandler extends Handler {

    static SdkHandler INSTANCE = new SdkHandler();

    public static final int CARDS_READY = 0;
    public static final int DELETE_CARD = 1;
    public static final int PAYMENT_INIT_COMPLETED = 2;

    public SdkHandler() {
        super(Looper.getMainLooper());
    }

    private Set<PayFormActivity> callbacks = new HashSet<>();

    public void register(PayFormActivity activity) {
        callbacks.add(activity);
    }

    public void unregister(PayFormActivity activity) {
        callbacks.remove(activity);
    }

    @Override
    public void handleMessage(Message msg) {
        int action = msg.what;

        switch (action) {
            case CARDS_READY:
                for (PayFormActivity activity : callbacks) {
                    activity.onCardsReady((Card[]) msg.obj);
                }
                return;
            case DELETE_CARD:
                for (PayFormActivity activity : callbacks) {
                    activity.onDeleteCard((Card) msg.obj);
                }
                return;
            case PAYMENT_INIT_COMPLETED:
                for (PayFormActivity activity : callbacks) {
                    activity.onPaymentInitCompleted((Long) msg.obj);
                }
                return;
        }
        super.handleMessage(msg);
    }
}
