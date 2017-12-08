package ru.tinkoff.acquiring.sdk;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;

import com.google.android.gms.wallet.WalletConstants;
import com.google.android.gms.wallet.fragment.WalletFragmentStyle;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Alex Maksakov
 */
final public class AndroidPayParams implements Parcelable {

    public static final Creator<AndroidPayParams> CREATOR = new Creator<AndroidPayParams>() {
        @Override
        public AndroidPayParams createFromParcel(Parcel in) {
            return new AndroidPayParams(in);
        }

        @Override
        public AndroidPayParams[] newArray(int size) {
            return new AndroidPayParams[size];
        }
    };

    @IntDef({WalletConstants.ENVIRONMENT_SANDBOX, WalletConstants.ENVIRONMENT_PRODUCTION, WalletConstants.ENVIRONMENT_STRICT_SANDBOX, WalletConstants.ENVIRONMENT_TEST})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AndroidPayEnvironment {
    }

    @IntDef({WalletFragmentStyle.BuyButtonText.BUY_WITH, WalletFragmentStyle.BuyButtonText.DONATE_WITH, WalletFragmentStyle.BuyButtonText.LOGO_ONLY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AndroidPayButtonText {
    }

    @IntDef({WalletFragmentStyle.BuyButtonAppearance.GOOGLE_WALLET_CLASSIC, WalletFragmentStyle.BuyButtonAppearance.GOOGLE_WALLET_GRAYSCALE, WalletFragmentStyle.BuyButtonAppearance.GOOGLE_WALLET_MONOCHROME,
            WalletFragmentStyle.BuyButtonAppearance.ANDROID_PAY_DARK, WalletFragmentStyle.BuyButtonAppearance.ANDROID_PAY_LIGHT, WalletFragmentStyle.BuyButtonAppearance.ANDROID_PAY_LIGHT_WITH_BORDER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AndroidPayButtonAppearance {
    }

    public static class Builder {

        private String merchantName = "";
        private String countryCode = "RU";
        private boolean isPhoneRequired = false;
        private boolean isAddressRequired = false;
        private int environment = WalletConstants.ENVIRONMENT_TEST;
        private int buyButtonText = WalletFragmentStyle.BuyButtonText.BUY_WITH;
        private int buyButtonAppearance = WalletFragmentStyle.BuyButtonAppearance.ANDROID_PAY_DARK;
        private int theme = WalletConstants.THEME_LIGHT;

        public Builder setCountryCode(String countryCode) {
            this.countryCode = countryCode;
            return this;
        }

        public Builder setPhoneRequired(boolean phoneRequired) {
            this.isPhoneRequired = phoneRequired;
            return this;
        }

        public Builder setAddressRequired(boolean addressRequired) {
            this.isAddressRequired = addressRequired;
            return this;
        }

        public Builder setEnvironment(@AndroidPayEnvironment int environment) {
            this.environment = environment;
            return this;
        }

        public Builder setBuyButtonText(@AndroidPayButtonText int buyButtonText) {
            this.buyButtonText = buyButtonText;
            return this;
        }

        public Builder setBuyButtonAppearance(@AndroidPayButtonAppearance int buyButtonAppearance) {
            this.buyButtonAppearance = buyButtonAppearance;
            return this;
        }

        public Builder setTheme(int theme) {
            this.theme = theme;
            return this;
        }

        public Builder setMerchantName(String merchantName) {
            this.merchantName = merchantName;
            return this;
        }

        public AndroidPayParams build() {
            AndroidPayParams params = new AndroidPayParams();
            params.merchantName = this.merchantName;
            params.countryCode = this.countryCode;
            params.isPhoneRequired = this.isPhoneRequired;
            params.isAddressRequired = this.isAddressRequired;
            params.environment = this.environment;
            params.buyButtonText = this.buyButtonText;
            params.buyButtonAppearance = this.buyButtonAppearance;
            params.theme = theme;
            return params;
        }
    }

    static final String PUBLIC_KEY = "BGxaSIxnXznhoT7f/bzVCGtlscPZO4QMIhRgV9JXqACgOy6iaf8PijQxLH64uRevV0rxT0EseFmJOzpUSk6NwJI=";
    static final String CURRENCY_CODE = "RUB";

    private String merchantName;
    private String countryCode;
    private boolean isPhoneRequired;
    private boolean isAddressRequired;
    private int environment;
    private int buyButtonText;
    private int buyButtonAppearance;
    private int theme;

    private AndroidPayParams() {

    }

    private AndroidPayParams(Parcel in) {
        merchantName = in.readString();
        countryCode = in.readString();
        isPhoneRequired = in.readByte() != 0;
        isAddressRequired = in.readByte() != 0;
        environment = in.readInt();
        buyButtonText = in.readInt();
        buyButtonAppearance = in.readInt();
        theme = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(merchantName);
        dest.writeString(countryCode);
        dest.writeByte((byte) (isPhoneRequired ? 1 : 0));
        dest.writeByte((byte) (isAddressRequired ? 1 : 0));
        dest.writeInt(environment);
        dest.writeInt(buyButtonText);
        dest.writeInt(buyButtonAppearance);
        dest.writeInt(theme);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public String getCountryCode() {
        return countryCode;
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

    public int getBuyButtonText() {
        return buyButtonText;
    }

    public int getBuyButtonAppearance() {
        return buyButtonAppearance;
    }

    public int getTheme() {
        return theme;
    }


}
