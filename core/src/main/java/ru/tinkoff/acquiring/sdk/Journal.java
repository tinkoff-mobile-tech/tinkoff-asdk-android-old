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

/**
 * @author Mikhail Artemyev
 */
public abstract class Journal {
    private static Logger logger = new JavaLogger();
    private static boolean debug = false;
    private static boolean developerMode = false;

    /**
     * Позволяет использовать свой логгер
     */
    public static void setLogger(Logger logger) {
        Journal.logger = logger;
    }

    /**
     * Позволяет включить логирование. По умолчанию выключен
     */
    public static void setDebug(boolean debug) {
        Journal.debug = debug;
    }

    public static boolean isDebug() {
        return Journal.debug;
    }

    /**
     * Позволяет переключать SDK с тестового режима и обратно. В тестовом режиме деньги с карты не
     * списываются. По умолчанию выключен.
     */
    public static void setDeveloperMode(boolean mode) {
        Journal.developerMode = mode;
    }

    public static boolean isDeveloperMode() {
        return Journal.developerMode;
    }

    public static void log(CharSequence message) {
        if (debug) {
            logger.log(message);
        }
    }

    public static void log(Throwable e) {
        if (debug) {
            logger.log(e);
        }
    }
}
