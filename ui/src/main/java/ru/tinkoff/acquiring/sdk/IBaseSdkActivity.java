package ru.tinkoff.acquiring.sdk;

import androidx.annotation.Nullable;

import ru.tinkoff.acquiring.sdk.responses.Check3dsVersionResponse;

/**
 * @author Vitaliy Markus
 */
public interface IBaseSdkActivity {

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
