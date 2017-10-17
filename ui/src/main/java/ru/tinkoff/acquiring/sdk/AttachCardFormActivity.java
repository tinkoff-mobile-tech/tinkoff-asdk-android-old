package ru.tinkoff.acquiring.sdk;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.MenuItem;

/**
 * @author Vitaliy Markus
 */
public class AttachCardFormActivity extends AppCompatActivity implements IAttachCardFormActivity {

    public static final int RESULT_ERROR = 500;
    static final String EXTRA_ERROR = "error";

    static final String EXTRA_TERMINAL_KEY = "terminal_key";
    static final String EXTRA_PASSWORD = "password";
    static final String EXTRA_PUBLIC_KEY = "public_key";

    static final String EXTRA_CUSTOMER_KEY = "customer_key";
    static final String EXTRA_CHECK_TYPE = "check_type";
    static final String EXTRA_CUSTOM_KEYBOARD = "keyboard";
    static final String EXTRA_DATA = "data";
    static final String EXTRA_THEME = "theme";
    static final String EXTRA_CAMERA_CARD_SCANNER = "card_scanner";
    static final String EXTRA_CARD_ID = "card_id";

    private DialogsManager dialogsManager;
    private AcquiringSdk sdk;

    private boolean useCustomKeyboard;
    private String cardId;

    public static AttachCardFormStarter init(String terminalKey, String password, String publicKey) {
        return new AttachCardFormStarter(terminalKey, password, publicKey);
    }

    public static void dispatchResult(int resultCode, Intent data, OnAttachCardListener listener) {
        if (resultCode == RESULT_OK) {
            listener.onSuccess(data.getStringExtra(AttachCardFormActivity.EXTRA_CARD_ID));
        } else if (resultCode == RESULT_CANCELED) {
            listener.onCancelled();
        } else if (resultCode == RESULT_ERROR) {
            listener.onError((Exception) data.getSerializableExtra(EXTRA_ERROR));
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        initActivity(intent);
        dialogsManager = new DialogsManager(this);
        sdk = createSdk(intent);
        useCustomKeyboard = intent.getBooleanExtra(EXTRA_CUSTOM_KEYBOARD, false);

        if (savedInstanceState == null) {
            addAttachCardFragment();
        } else {

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        CommonSdkHandler.INSTANCE.register(this);
        AttachCardFormHandler.INSTANCE.register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        dialogsManager.dismissDialogs();
        CommonSdkHandler.INSTANCE.unregister(this);
        AttachCardFormHandler.INSTANCE.unregister(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            navigateBack();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void success() {
        hideProgressDialog();
        Intent data = new Intent();
        data.putExtra(EXTRA_CARD_ID, cardId);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void cancel() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    @Override
    public void showProgressDialog() {
        dialogsManager.showProgressDialog(getString(R.string.acq_progress_dialog_attach_card_text));
    }

    @Override
    public void hideProgressDialog() {
        dialogsManager.hideProgressDialog();
    }

    @Override
    public void exception(Exception e) {
        hideProgressDialog();
        Intent data = new Intent();
        data.putExtra(EXTRA_ERROR, e);
        setResult(RESULT_ERROR, data);
        finish();
    }

    @Override
    public void start3DS(ThreeDsData data) {
        hideProgressDialog();
        Fragment fragment = new ThreeDsFragment();
        Bundle args = new Bundle();
        args.putBundle(ThreeDsFragment.EXTRA_3DS, new ThreeDsBundlePacker().pack(data));
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }

    @Override
    public void showErrorDialog(Exception e) {
        String message = e.getMessage();
        if (TextUtils.isEmpty(message)) {
            message = getString(R.string.acq_default_error_message);
        }
        dialogsManager.showErrorDialog(getString(R.string.acq_default_error_title), message);
    }

    @Override
    public void noNetwork() {
        String title = getString(R.string.acq_default_error_title);
        String message = getString(R.string.acq_network_error_message);
        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setResult(RESULT_CANCELED);
                AttachCardFormActivity.this.finish();
            }
        };
        dialogsManager.showErrorDialog(title, message, onClickListener);
        hideProgressDialog();
    }

    @Override
    public void onAttachCardId(String cardId) {
        this.cardId = cardId;
    }

    @Override
    public void showLoopConfirmations(String requestKey) {
        hideProgressDialog();
        Fragment fragment = LoopConfirmationFragment.newInstance(requestKey);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .addToBackStack("loop_confirmation")
                .commit();
    }

    @Override
    public void onBackPressed() {
        final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (fragment instanceof OnBackPressedListener) {
            final OnBackPressedListener listener = ((OnBackPressedListener) fragment);
            if (listener.onBackPressed()) {
                return;
            }
        }
        navigateBack();
    }

    @Override
    public AcquiringSdk getSdk() {
        return sdk;
    }

    boolean shouldUseCustomKeyboard() {
        return useCustomKeyboard;
    }

    private void initActivity(Intent intent) {
        int theme = intent.getIntExtra(EXTRA_THEME, 0);
        if (theme != 0) {
            setTheme(theme);
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.acq_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TypedValue tv = new TypedValue();
        getTheme().resolveAttribute(R.attr.acqPayFormTitle, tv, true);
        String title = getResources().getString(tv.resourceId);
        setTitle(title);
    }

    private AcquiringSdk createSdk(Intent intent) {
        String terminalKey = intent.getStringExtra(EXTRA_TERMINAL_KEY);
        String password = intent.getStringExtra(EXTRA_PASSWORD);
        String publicKey = intent.getStringExtra(EXTRA_PUBLIC_KEY);
        return new AcquiringSdk(terminalKey, password, publicKey);
    }

    private void navigateBack() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            setResult(RESULT_CANCELED);
        }
        super.onBackPressed();
    }

    private void addAttachCardFragment() {
        Fragment fragment = AttachCardFormFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

    }
}
