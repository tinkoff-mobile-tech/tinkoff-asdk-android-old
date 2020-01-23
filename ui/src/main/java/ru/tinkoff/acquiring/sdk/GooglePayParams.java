package ru.tinkoff.acquiring.sdk;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.IntDef;

import com.google.android.gms.wallet.WalletConstants;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Alex Maksakov
 */
final public class GooglePayParams implements Parcelable {

    public static final Creator<GooglePayParams> CREATOR = new Creator<GooglePayParams>() {
        @Override
        public GooglePayParams createFromParcel(Parcel in) {
            return new GooglePayParams(in);
        }

        @Override
        public GooglePayParams[] newArray(int size) {
            return new GooglePayParams[size];
        }
    };

    @IntDef({WalletConstants.ENVIRONMENT_SANDBOX, WalletConstants.ENVIRONMENT_PRODUCTION, WalletConstants.ENVIRONMENT_STRICT_SANDBOX, WalletConstants.ENVIRONMENT_TEST})
    @Retention(RetentionPolicy.SOURCE)
    public @interface GooglePayEnvironment {
    }

    @Deprecated
    @IntDef({ANDROID_PAY_DARK, ANDROID_PAY_LIGHT_WITH_BORDER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface GooglePayButtonAppearance {
    }

    public static class Builder {

        private boolean isPhoneRequired = false;
        private boolean isAddressRequired = false;
        private int environment = WalletConstants.ENVIRONMENT_TEST;
        private int buyButtonAppearance = -1;
        private int theme = WalletConstants.THEME_LIGHT;


        public Builder setPhoneRequired(boolean phoneRequired) {
            this.isPhoneRequired = phoneRequired;
            return this;
        }

        public Builder setAddressRequired(boolean addressRequired) {
            this.isAddressRequired = addressRequired;
            return this;
        }

        public Builder setEnvironment(@GooglePayEnvironment int environment) {
            this.environment = environment;
            return this;
        }

        public Builder setBuyButtonAppearance(@GooglePayButtonAppearance int buyButtonAppearance) {
            this.buyButtonAppearance = buyButtonAppearance;
            return this;
        }

        public Builder setTheme(int theme) {
            this.theme = theme;
            return this;
        }

        public GooglePayParams build() {
            GooglePayParams params = new GooglePayParams();
            params.isPhoneRequired = this.isPhoneRequired;
            params.isAddressRequired = this.isAddressRequired;
            params.environment = this.environment;
            params.buyButtonAppearance = this.buyButtonAppearance;
            params.theme = theme;
            return params;
        }
    }

    public static final String CURRENCY_CODE = "RUB";

    @Deprecated
    public static final int ANDROID_PAY_DARK = 4;

    @Deprecated
    public static final int ANDROID_PAY_LIGHT_WITH_BORDER = 6;

    private boolean isPhoneRequired;
    private boolean isAddressRequired;
    private int environment;
    private int buyButtonAppearance;
    private int theme;

    private GooglePayParams() {

    }

    private GooglePayParams(Parcel in) {
        isPhoneRequired = in.readByte() != 0;
        isAddressRequired = in.readByte() != 0;
        environment = in.readInt();
        buyButtonAppearance = in.readInt();
        theme = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isPhoneRequired ? 1 : 0));
        dest.writeByte((byte) (isAddressRequired ? 1 : 0));
        dest.writeInt(environment);
        dest.writeInt(buyButtonAppearance);
        dest.writeInt(theme);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean isPhoneRequired() {
        return isPhoneRequired;
    }

    public boolean isAddressRequired() {
        return isAddressRequired;
    }

    public int getEnvironment() {
        return environment;
    }

    public int getBuyButtonAppearance() {
        return buyButtonAppearance;
    }

    public int getTheme() {
        return theme;
    }
}
