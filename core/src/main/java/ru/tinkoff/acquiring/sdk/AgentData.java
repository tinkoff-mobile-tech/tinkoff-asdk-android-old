package ru.tinkoff.acquiring.sdk;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AgentData implements Serializable {

    @SerializedName("AgentSign")
    private AgentSign agentSign;

    @SerializedName("OperationName")
    private String operationName;

    @SerializedName("Phones")
    private String[] phones;

    @SerializedName("ReceiverPhones")
    private String[] receiverPhones;

    @SerializedName("TransferPhones")
    private String[] transferPhones;

    @SerializedName("OperatorName")
    private String operatorName;

    @SerializedName("OperatorAddress")
    private String operatorAddress;

    @SerializedName("OperatorInn")
    private String operatorInn;

    public AgentSign getAgentSign() {
        return agentSign;
    }

    public void setAgentSign(AgentSign agentSign) {
        this.agentSign = agentSign;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public String[] getPhones() {
        return phones;
    }

    public void setPhones(String[] phones) {
        this.phones = phones;
    }

    public String[] getReceiverPhones() {
        return receiverPhones;
    }

    public void setReceiverPhones(String[] receiverPhones) {
        this.receiverPhones = receiverPhones;
    }

    public String[] getTransferPhones() {
        return transferPhones;
    }

    public void setTransferPhones(String[] transferPhones) {
        this.transferPhones = transferPhones;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getOperatorAddress() {
        return operatorAddress;
    }

    public void setOperatorAddress(String operatorAddress) {
        this.operatorAddress = operatorAddress;
    }

    public String getOperatorInn() {
        return operatorInn;
    }

    public void setOperatorInn(String operatorInn) {
        this.operatorInn = operatorInn;
    }

}
