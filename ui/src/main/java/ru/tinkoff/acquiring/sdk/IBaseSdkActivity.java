package ru.tinkoff.acquiring.sdk;

import ru.tinkoff.acquiring.sdk.localization.HasAsdkLocalization;

/**
 * @author Vitaliy Markus
 */
public interface IBaseSdkActivity extends HasAsdkLocalization {

    void success();

    void cancel();

    void showProgressDialog();

    void hideProgressDialog();

    void exception(Throwable throwable);

    void start3DS(ThreeDsData data);

    void showErrorDialog(Exception e);

    void noNetwork();

    AcquiringSdk getSdk();
}
