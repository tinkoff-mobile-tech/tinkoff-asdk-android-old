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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.SparseArray;

import java.lang.ref.WeakReference;
import java.util.regex.Pattern;

/**
 * @author a.shishkin1
 */



class CardLogoCache {

    private static final Pattern MIR_REGEXP = Pattern.compile("^220[0-4]");

    private static SparseArray<WeakReference<Bitmap>> sCache = new SparseArray<>();

    private int visaId;
    private int masterCardId;
    private int maestroId;
    private int mirId;

    public CardLogoCache(int visaId, int masterCardId, int maestroId, int mirId) {
        this.visaId = visaId;
        this.masterCardId = masterCardId;
        this.maestroId = maestroId;
        this.mirId = mirId;
    }

    public CardLogoCache() {
    }

    protected void setVisaId(int visaId) {
        this.visaId = visaId;
    }

    protected void setMasterCardId(int masterCardId) {
        this.masterCardId = masterCardId;
    }

    protected void setMaestroId(int maestroId) {
        this.maestroId = maestroId;
    }

    protected void setMirId(int mirId) {
        this.mirId = mirId;
    }

    public Bitmap getLogoByNumber(Context context, String cardNumber) {
        if (TextUtils.isEmpty(cardNumber)) {
            return null;
        }
        int resId = resByNumber(cardNumber);

        if(resId == 0) {
            return null;
        }
        Bitmap result;
        WeakReference<Bitmap> weakReference = sCache.get(resId);
        if(weakReference != null) {
            result = weakReference.get();
            if(result != null) {
                return result;
            }
        }
        result = BitmapFactory.decodeResource(context.getResources(), resId);
        sCache.put(resId, new WeakReference<>(result));
        return result;
    }

    private int resByNumber(String cardNumber) {
        if (MIR_REGEXP.matcher(cardNumber).find()) {
            return mirId;
        } else {
            char ch = cardNumber.charAt(0);
            return resByChar(ch);
        }
    }

    private int resByChar(char c) {
        switch (c) {
            case '2':
            case '5':
                return masterCardId;
            case '4':
                return visaId;
            case '6':
                return maestroId;
        }
        return 0;
    }
}

