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
import android.util.TypedValue;


/**
 * @author a.shishkin1
 */


class ThemeCardLogoCache extends CardLogoCache {

    public ThemeCardLogoCache(Context context) {
        super();
        int[] attributes = new int [] { R.attr.acqMaestroIcon, R.attr.acqMasterCardIcon, R.attr.acqVisaIcon  };
        TypedValue tv = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.acqCardIcons, tv, true);

        TypedArray array = null;
        try {
            array = context.obtainStyledAttributes(tv.resourceId, attributes);
            int maestroId = array.getResourceId(0, 0);
            int masterId = array.getResourceId(1, 0);
            int visaId = array.getResourceId(2, 0);

            setMaestroId(maestroId);
            setVisaId(visaId);
            setMasterCardId(masterId);

        } catch (Exception e) {
           Journal.log(e);
        } finally {
            if (array != null) {
                array.recycle();
            }
        }
    }

}
