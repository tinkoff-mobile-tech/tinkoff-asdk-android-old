package ru.tinkoff.acquiring.sdk;

/**
 * @author a.shishkin1
 */
public class TAcqIntentExtra {
    
    public static final String EXTRA_TERMINAL_KEY = "terminal_key";
    public static final String EXTRA_PASSWORD = "password";
    public static final String EXTRA_PUBLIC_KEY = "public_key";
    public static final String EXTRA_ORDER_ID = "order_id";
    public static final String EXTRA_AMOUNT = "amount";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_DESCRIPTION = "description";
    public static final String EXTRA_CARD_ID = "card_id";
    public static final String EXTRA_E_MAIL = "email";
    public static final String EXTRA_CUSTOM_KEYBOARD = "keyboard";
    public static final String EXTRA_CUSTOMER_KEY = "customer_key";
    public static final String EXTRA_RECURRENT_PAYMENT = "recurrent_payment";
    public static final String EXTRA_PAYMENT_ID = "payment_id";
    public static final String EXTRA_RECEIPT_VALUE = "receipt_value";
    public static final String EXTRA_DATA_VALUE = "data_value";
    public static final String EXTRA_CHARGE_MODE = "charge_mode";
    public static final String EXTRA_USE_FIRST_ATTACHED_CARD = "use_first_saved_card";
    public static final String EXTRA_THEME = "theme";
    public static final String EXTRA_CAMERA_CARD_SCANNER = "card_scanner";
    public static final String EXTRA_DESIGN_CONFIGURATION = "design_configuration";
    public static final String EXTRA_ANDROID_PAY_PARAMS = "android_pay_params";
    public static final String EXTRA_CARD_DATA = "extra_card_data";
    public static final String EXTRA_PAYMENT_INFO = "extra_payment_info";
    public static final String EXTRA_THREE_DS = "extra_three_ds";
    public static final String EXTRA_CHECK_TYPE = "check_type";
    public static final String EXTRA_DATA = "data";
    public static final String EXTRA_LANGUAGE = "language";
    public static final String EXTRA_LOCALIZATION_RAW_RESOURCE_ID = "localizationRawResourceId";
    public static final String EXTRA_LOCALIZATION_FILE_PATH = "localizationFilePath";
    public static final String EXTRA_ERROR = "error";
    
    private TAcqIntentExtra () {
        throw new AssertionError("no TAcqIntentExtra instances");
    }
}
