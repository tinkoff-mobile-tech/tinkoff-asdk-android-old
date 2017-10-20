package ru.tinkoff.acquiring.sdk;

/**
 * @author Vitaliy Markus
 */
public class CreditCard implements ICreditCard {

    private final String cardNumber;
    private final String expireDate;
    private final String cardholderName;

    public CreditCard(String cardNumber, String expireDate, String cardholderName) {
        this.cardNumber = cardNumber;
        this.expireDate = expireDate;
        this.cardholderName = cardholderName;
    }

    @Override
    public String getCardNumber() {
        return cardNumber;
    }

    @Override
    public String getExpireDate() {
        return expireDate;
    }

    @Override
    public String getCardholderName() {
        return cardholderName;
    }
}
