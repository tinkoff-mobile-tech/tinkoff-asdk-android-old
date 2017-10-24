package ru.tinkoff.acquiring.sample.camera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.Random;

import ru.tinkoff.acquiring.sample.R;

/**
 * @author Vitaliy Markus
 */
public class DemoCameraScanActivity extends AppCompatActivity {

    public static final String EXTRA_CARD_NUMBER = "card_number";
    public static final String EXTRA_EXPIRE_DATE = "expire_date";

    private static final String[] CARD_NUMBERS = {"5136 9149 2034 4072", "5136 9149 2034 7240", "5203 7375 0075 0535", "5203 7375 0075 3505"};
    private static final String EXPIRE_DATE = "11/17";
    private static final Random random = new Random();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_demo);
        findViewById(R.id.bt_success).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra(EXTRA_CARD_NUMBER, CARD_NUMBERS[random.nextInt(CARD_NUMBERS.length)]);
                data.putExtra(EXTRA_EXPIRE_DATE, EXPIRE_DATE);
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        });

        findViewById(R.id.bt_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });
    }
}
