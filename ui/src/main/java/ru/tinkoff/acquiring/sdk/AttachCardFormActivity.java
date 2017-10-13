package ru.tinkoff.acquiring.sdk;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.MenuItem;

/**
 * @author Vitaliy Markus
 */
public class AttachCardFormActivity extends AppCompatActivity {

    static final String EXTRA_TERMINAL_KEY = "terminal_key";
    static final String EXTRA_PASSWORD = "password";
    static final String EXTRA_PUBLIC_KEY = "public_key";

    static final String EXTRA_CUSTOMER_KEY = "customer_key";
    static final String EXTRA_CUSTOM_KEYBOARD = "keyboard";
    static final String EXTRA_DATA = "data";
    static final String EXTRA_THEME = "theme";

    private DialogsManager dialogsManager;
    private AcquiringSdk sdk;
    private boolean useCustomKeyboard;

    public static AttachCardFormStarter init(String terminalKey, String password, String publicKey) {
        return new AttachCardFormStarter(terminalKey, password, publicKey);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            navigateBack();
            return true;
        }

        return super.onOptionsItemSelected(item);
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
