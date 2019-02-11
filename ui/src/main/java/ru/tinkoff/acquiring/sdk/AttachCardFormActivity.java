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
import android.view.MenuItem;
import android.widget.Toast;

import ru.tinkoff.acquiring.sdk.localization.AsdkLocalization;
import ru.tinkoff.acquiring.sdk.localization.HasAsdkLocalization;

/**
 * @author Vitaliy Markus
 */
public class AttachCardFormActivity extends AppCompatActivity implements IAttachCardFormActivity {

    public static final int RESULT_ERROR = 500;

    private DialogsManager dialogsManager;
    private AcquiringSdk sdk;

    private boolean useCustomKeyboard;
    private String cardId;
    private AsdkLocalizationProperty asdkLocalization;

    public static AttachCardFormStarter init(String terminalKey, String password, String publicKey) {
        return new AttachCardFormStarter(terminalKey, password, publicKey);
    }

    public static void dispatchResult(int resultCode, Intent data, OnAttachCardListener listener) {
        if (resultCode == RESULT_OK) {
            listener.onSuccess(data.getStringExtra(TAcqIntentExtra.EXTRA_CARD_ID));
        } else if (resultCode == RESULT_CANCELED) {
            listener.onCancelled();
        } else if (resultCode == RESULT_ERROR) {
            listener.onError((Exception) data.getSerializableExtra(TAcqIntentExtra.EXTRA_ERROR));
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        asdkLocalization = new AsdkLocalizationProperty(this);
        Intent intent = getIntent();
        initActivity(intent);
        dialogsManager = new DialogsManager(this);
        sdk = createSdk(intent);
        useCustomKeyboard = intent.getBooleanExtra(TAcqIntentExtra.EXTRA_CUSTOM_KEYBOARD, false);

        if (savedInstanceState == null) {
            addAttachCardFragment();
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
        data.putExtra(TAcqIntentExtra.EXTRA_CARD_ID, cardId);
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
        dialogsManager.showProgressDialog(getAsdkLocalization().addCardDialogProgressAddCardMessage);
    }

    @Override
    public void hideProgressDialog() {
        dialogsManager.hideProgressDialog();
    }

    @Override
    public void exception(Throwable throwable) {
        hideProgressDialog();

        if (throwable instanceof AcquiringApiException) {
            Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_LONG).show();
        }

        Intent data = new Intent();
        data.putExtra(TAcqIntentExtra.EXTRA_ERROR, throwable);
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
            message = getAsdkLocalization().addCardDialogErrorFallbackMessage;
        }
        dialogsManager.showErrorDialog(getAsdkLocalization().addCardDialogErrorTitle, message);
    }

    @Override
    public void noNetwork() {
        String title = getAsdkLocalization().addCardDialogErrorTitle;
        String message = getAsdkLocalization().addCardDialogErrorNetwork;
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
        Fragment fragment = createLoopConfirmationFragment(requestKey);
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

    protected LoopConfirmationFragment createLoopConfirmationFragment(String requestKey) {
        return LoopConfirmationFragment.newInstance(requestKey);
    }

    protected AttachCardFormFragment createAttachCardFormFragment() {
        return AttachCardFormFragment.newInstance();
    }

    boolean shouldUseCustomKeyboard() {
        return useCustomKeyboard;
    }

    private void initActivity(Intent intent) {
        int theme = intent.getIntExtra(TAcqIntentExtra.EXTRA_THEME, 0);
        if (theme != 0) {
            setTheme(theme);
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.acq_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(getAsdkLocalization().addCardTitle);
    }

    private AcquiringSdk createSdk(Intent intent) {
        String terminalKey = intent.getStringExtra(TAcqIntentExtra.EXTRA_TERMINAL_KEY);
        String password = intent.getStringExtra(TAcqIntentExtra.EXTRA_PASSWORD);
        String publicKey = intent.getStringExtra(TAcqIntentExtra.EXTRA_PUBLIC_KEY);
        return new AcquiringSdk(terminalKey, password, publicKey);
    }

    private void navigateBack() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            setResult(RESULT_CANCELED);
        }
        super.onBackPressed();
    }

    private void addAttachCardFragment() {
        Fragment fragment = createAttachCardFormFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

    }

    @Override
    public AsdkLocalization getAsdkLocalization() {
        return asdkLocalization.getAsdkLocalization();
    }
}
