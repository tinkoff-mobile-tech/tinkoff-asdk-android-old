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

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import ru.tinkoff.acquiring.sdk.utils.Base64;

/**
 * @author a.shishkin1
 */
public class StringKeyCreator implements KeyCreator {

    private String source;

    public StringKeyCreator() {
    }

    public StringKeyCreator(String source) {
        this.source = source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public PublicKey create() {
        if (source == null) {
            throw new NullPointerException("StringKeyCreator can't create Key, source String is null, use setSource(String)");
        }
        try {
            final byte[] publicBytes = Base64.decode(source, Base64.DEFAULT);
            final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
            final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new AcquiringSdkException(e);
        }
    }
}
