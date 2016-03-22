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

package ru.tinkoff.acquiring.sdk.responses;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @author Mikhail Artemyev
 */
public class AcquiringResponse implements Serializable {

    @SerializedName("ErrorCode")
    private String errorCode;

    @SerializedName("Message")
    private String message;

    @SerializedName("Details")
    private String details;

    @SerializedName("Success")
    private boolean success;

    @SerializedName("TerminalKey")
    private String terminalKey;

    public AcquiringResponse() {
    }

    protected AcquiringResponse(String errorCode, boolean success) {
        this.errorCode = errorCode;
        this.success = success;
    }

    public String getTerminalKey() {
        return terminalKey;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public String getDetails() {
        return details;
    }
}

