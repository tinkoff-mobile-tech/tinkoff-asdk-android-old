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

import android.annotation.TargetApi;
import android.os.Build;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author a.shishkin1
 */


class Utils {

    private Utils() {
        throw new AssertionError("No Utils instances for you");
    }


    public static void closeCloseable(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                Journal.log(e);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void closeCloseable(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                Journal.log(e);
            }
        }
    }


    public static void closeAllCloseables(Object... args) {
        for (Object current : args) {
            if (current instanceof Closeable) {
                closeCloseable((Closeable) current);
            } else if (current instanceof AutoCloseable) {
                closeCloseable((AutoCloseable) current);
            } else {
                String msg = "close not supported for " + ((current == null) ? "null" : current.getClass().getCanonicalName());
                Journal.log(new UnsupportedOperationException(new IllegalArgumentException(msg)));
            }
        }
    }

}
