package ru.tinkoff.acquiring.sdk;

import com.google.gson.annotations.SerializedName;

public enum AgentSign {

    @SerializedName("bank_paying_agent")
    BANK_PAYING_AGENT,
    @SerializedName("bank_paying_subagent")
    BANK_PAYING_SUBAGENT,
    @SerializedName("paying_agent")
    PAYING_AGENT,
    @SerializedName("paying_subagent")
    PAYING_SUBAGENT,
    @SerializedName("attorney")
    ATTORNEY,
    @SerializedName("commission_agent")
    COMMISSION_AGENT,
    @SerializedName("another")
    ANOTHER

}
