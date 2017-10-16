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
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.util.TypedValue;

import java.util.Arrays;

import ru.tinkoff.acquiring.sdk.views.EditCardView;


/**
 * @author a.shishkin1
 */
class ThemeCardLogoCache extends CardLogoCache implements EditCardView.CardSystemIconsHolder {

    private Context context;

    public ThemeCardLogoCache(Context context) {
        super();
        this.context = context;
        int[] attributes = new int[]{
                R.attr.acqMaestroIcon,
                R.attr.acqMasterCardIcon,
                R.attr.acqMirIcon,
                R.attr.acqVisaIcon
        };
        Arrays.sort(attributes);
        TypedValue tv = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.acqCardIcons, tv, true);

        TypedArray array = context.obtainStyledAttributes(tv.resourceId, attributes);
        int maestroId = array.getResourceId(indexOf(R.attr.acqMaestroIcon, attributes), 0);
        int masterId = array.getResourceId(indexOf(R.attr.acqMasterCardIcon, attributes), 0);
        int mirId = array.getResourceId(indexOf(R.attr.acqMirIcon, attributes), 0);
        int visaId = array.getResourceId(indexOf(R.attr.acqVisaIcon, attributes), 0);

        setMaestroId(maestroId);
        setVisaId(visaId);
        setMasterCardId(masterId);
        setMirId(mirId);

        array.recycle();
    }

    @Override
    public Bitmap getCardSystemBitmap(String cardNumber) {
        return getLogoByNumber(context, cardNumber);
    }

    private int indexOf(int id, int[] attributes) {
        for (int i = 0; i < attributes.length; i++) {
            if (attributes[i] == id) {
                return i;
            }
        }
        throw new RuntimeException("Failed to find attribute " + id);
    }
}
