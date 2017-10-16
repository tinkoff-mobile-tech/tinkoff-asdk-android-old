package ru.tinkoff.acquiring.sdk;

/**
 * @author Vitaliy Markus
 */
public class CreditCard implements ICreditCard {

    private final String cardNumber;
    private final String expireDate;
    private final String cardholderName;

    public CreditCard(String cardNumber, String expireDate, String cvc) {
        this.cardNumber = cardNumber;
        this.expireDate = expireDate;
        this.cardholderName = cvc;
    }

    @Override
    public String getCardNumber() {
        return cardNumber;
    }

    @Override
    public String getExpireDate() {
        return expireDate;
    }

    public String getCardholderName() {
        return cardholderName;
    }
}
