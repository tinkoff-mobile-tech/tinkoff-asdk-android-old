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

import android.support.annotation.Nullable;

/**
 * Коллбек позволяющий получать уведомления о статусе проведения платежа
 *
 * @author a.shishkin1
 */
public interface OnPaymentListener {

    /**
     * Вызывается в случае, если платеж прошел успешно
     * @param paymentId - идентификатор платежа
     * @param cardId - идентификатор карты, присутствует если при оплате использовалась привязанная карта
     */
    void onSuccess(long paymentId, @Nullable String cardId);

    /**
     * Вызывается, если платеж отменен пользователем
     */
    void onCancelled();

    /**
     * Вызывается, если не удалось совершить платеж
     */
    void onError(Exception e);
}
