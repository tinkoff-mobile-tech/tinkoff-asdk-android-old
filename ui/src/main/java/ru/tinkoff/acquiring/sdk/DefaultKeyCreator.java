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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.PublicKey;

/**
 * @author a.shishkin1
 */
public class DefaultKeyCreator implements KeyCreator {

    private static final String FILE_NAME = "public.pem";

    private Context context;
    private StringKeyCreator stringKeyCreator;

    public DefaultKeyCreator(Context context) {
        this.context = context;
        this.stringKeyCreator = new StringKeyCreator();
    }

    @Override
    public PublicKey create() {
        InputStream is = null;
        BufferedReader br = null;
        try {
            is = context.getAssets().open(FILE_NAME);
            br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            sb.setLength(sb.length() - 1);
            String source = sb.toString();
            source = source.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "").trim();
            stringKeyCreator.setSource(source);
            return stringKeyCreator.create();
        } catch (Exception e) {
            throw new AcquiringSdkException(e);
        } finally {
            Utils.closeAllCloseables(br, is);
        }
    }
}
