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

import android.os.Bundle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

/**
 * @author a.shishkin1
 */
class CardsArrayBundlePacker implements IBundlePacker<Card[]> {


    private static final String COUNT = "count";
    private static final String DATA = "data";

    @Override
    public Card[] unpack(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        int count = bundle.getInt(COUNT, 0);
        if (count == 0) {
            return new Card[0];
        }
        byte[] src = bundle.getByteArray(DATA);
        Card[] result = new Card[count];
        fillArray(result, src);
        return result;
    }

    @Override
    public Bundle pack(Card[] entity) {
        if (entity == null) {
            return null;
        }
        Bundle result = new Bundle();
        int count = entity.length;
        result.putInt(COUNT, count);
        if (count > 0) {
            result.putByteArray(DATA, createBytes(entity));
        }
        return result;
    }

    private byte[] createBytes(Card[] arr) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            for (Card card : arr) {
                out.writeObject(card);
            }
            return bos.toByteArray();

        } catch (IOException e) {
            Journal.log(e);
        } finally {
            Utils.closeAllCloseables(bos, out);
        }
        return null;
    }

    private void fillArray(Card[] cards, byte[] source) {
        ByteArrayInputStream bis = new ByteArrayInputStream(source);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            for (int i = 0; i < cards.length; i++) {
                cards[i] = (Card) in.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            Journal.log(e);
        } finally {
            Utils.closeAllCloseables(bis, in);
        }
    }

}
