package ru.tinkoff.acquiring.sdk.localization;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * @author a.shishkin1
 */
class Utility {

    static String read(InputStream is) throws IOException {
        char[] buffer = new char[1024];
        StringBuilder result = new StringBuilder();
        Reader in = new InputStreamReader(is);
        int v;
        while((v = in.read(buffer, 0, buffer.length)) >= 0) {
            result.append(buffer, 0, v);
        }
        return result.toString();
    }

}
