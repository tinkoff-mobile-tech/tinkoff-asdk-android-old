package ru.tinkoff.acquiring.sdk;

/**
 * @author Vitaliy Markus
 */
public interface IAttachCardFormActivity extends IBaseSdkActivity {

    void onAttachCardId(String cardId);

    void showLoopConfirmations(String requestKey);
}
