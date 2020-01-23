package ru.tinkoff.acquiring.sdk;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * @author Vitaliy Markus
 */
public interface ICreditCard extends Serializable {

    @NonNull
    String getCardNumber();

    @NonNull
    String getExpireDate();

    @NonNull
    String getCardholderName();
}
