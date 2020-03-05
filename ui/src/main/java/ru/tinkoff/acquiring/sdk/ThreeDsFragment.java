/*
 * Copyright © 2016 Tinkoff Bank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.tinkoff.acquiring.sdk;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import ru.tinkoff.acquiring.sdk.responses.Check3dsVersionResponse;
import ru.tinkoff.acquiring.sdk.responses.GetAddCardStateResponse;
import ru.tinkoff.acquiring.sdk.utils.Base64;

/**
 * @author a.shishkin1
 */
public class ThreeDsFragment extends Fragment {

    public static final String EXTRA_3DS = "extra_3ds";

    private static final String SUBMIT_3DS_AUTHORIZATION = "Submit3DSAuthorization";
    private static final String SUBMIT_3DS_AUTHORIZATION_V2 = "Submit3DSAuthorizationV2";
    private static final String COMPLETE_3DS_METHOD_V2 = "Complete3DSMethodv2";

    private static final String THREE_DS_CALLED_FLAG = "Y";
    private static final String THREE_DS_NOT_CALLED_FLAG = "N";

    private static final String[] CANCEL_ACTIONS = new String[]{"cancel.do", "cancel=true"};

    private WebView wvThreeDs;
    private ThreeDsData data;
    private AcquiringSdk sdk;
    private String termUrl;

    public static ThreeDsFragment newInstance(Bundle threeDSBundle) {
        ThreeDsFragment fragment = new ThreeDsFragment();
        Bundle args = new Bundle();
        args.putBundle(EXTRA_3DS, threeDSBundle);
        fragment.setArguments(args);
        return fragment;
    }

    public static Map<String, String> collectData(Context context, @Nullable Check3dsVersionResponse response) {
        String threeDSCompInd = THREE_DS_NOT_CALLED_FLAG;
        if (response != null) {
            WebView hiddenWebView = new WebView(context);

            String notificationUrl = AcquiringApi.getUrl(COMPLETE_3DS_METHOD_V2) + "/" + COMPLETE_3DS_METHOD_V2;
            JSONObject threeDsMethodData = new JSONObject();
            try {
                threeDsMethodData.put("threeDSMethodNotificationURL", notificationUrl);
                threeDsMethodData.put("threeDSServerTransID", response.getServerTransId());
            } catch (JSONException e) {
                //ignore
            }

            String dataBase64 = Base64.encodeToString(threeDsMethodData.toString().getBytes(), Base64.DEFAULT).trim();
            String params;
            try {
                params = "threeDSMethodData=" + URLEncoder.encode(dataBase64, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new AcquiringSdkException(e);
            }

            hiddenWebView.postUrl(response.getThreeDsMethodUrl(), params.getBytes());
            threeDSCompInd = THREE_DS_CALLED_FLAG;
        }

        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);

        String cresCallbackUrl = AcquiringApi.getUrl(SUBMIT_3DS_AUTHORIZATION_V2) + "/" + SUBMIT_3DS_AUTHORIZATION_V2;

        Map<String, String> deviceData = new HashMap<>();
        deviceData.put("threeDSCompInd", threeDSCompInd);
        deviceData.put("language", Locale.getDefault().toString().replace("_", "-"));
        deviceData.put("timezone", getTimeZoneOffset());
        deviceData.put("screen_height", String.valueOf(point.y));
        deviceData.put("screen_width", String.valueOf(point.x));
        deviceData.put("cresCallbackUrl", cresCallbackUrl);

        return deviceData;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.acq_fragment_3ds, container, false);
        wvThreeDs = (WebView) view.findViewById(R.id.wv_3ds);
        wvThreeDs.setWebViewClient(new ThisWebViewClient());
        wvThreeDs.getSettings().setDomStorageEnabled(true);
        wvThreeDs.getSettings().setJavaScriptEnabled(true);
        wvThreeDs.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        data = new ThreeDsBundlePacker().unpack(getArguments().getBundle(EXTRA_3DS));
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sdk = ((IBaseSdkActivity) getActivity()).getSdk();
        String url = data.getAcsUrl();

        try {
            String params;
            if (data.getTdsServerTransId() != null) {
                termUrl = sdk.getUrl(SUBMIT_3DS_AUTHORIZATION_V2) + "/" + SUBMIT_3DS_AUTHORIZATION_V2;
                String base64 = prepareCreqParams();
                params = "creq=" + URLEncoder.encode(base64, "UTF-8");
            } else {
                termUrl = sdk.getUrl(SUBMIT_3DS_AUTHORIZATION) + "/" + SUBMIT_3DS_AUTHORIZATION;
                params = "PaReq=" + URLEncoder.encode(data.getPaReq(), "UTF-8") +
                        "&MD=" + URLEncoder.encode(data.getMd(), "UTF-8") +
                        "&TermUrl=" + URLEncoder.encode(termUrl, "UTF-8");
            }

            wvThreeDs.postUrl(url, params.getBytes());
        } catch (Exception e) {
            throw new AcquiringSdkException(e);
        }
    }

    private String prepareCreqParams() {
        JSONObject creqData = new JSONObject();
        try {
            creqData.put("threeDSServerTransID", data.getTdsServerTransId());
            creqData.put("acsTransID", data.getAcsTransId());
            creqData.put("messageVersion", data.getVersionName());
            creqData.put("challengeWindowSize", "05");
            creqData.put("messageType", "CReq");
        } catch (JSONException e) {
            //ignore
        }
        return Base64.encodeToString(creqData.toString().getBytes(), Base64.DEFAULT).trim();
    }

    private class ThisWebViewClient extends WebViewClient {

        boolean canceled = false;

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            for (String cancel : CANCEL_ACTIONS) {
                if (url.contains(cancel)) {
                    canceled = true;
                    Activity activity = (Activity) view.getContext();
                    activity.setResult(Activity.RESULT_CANCELED);
                    activity.finish();
                }
            }

            if (termUrl.equals(url)) {
                view.setVisibility(View.INVISIBLE);
                if (!canceled) {
                    requestState(sdk, data);
                }
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    private static void requestState(final AcquiringSdk sdk, final ThreeDsData threeDsData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (threeDsData.isPayment()) {
                        PaymentStatus status = sdk.getState(threeDsData.getPaymentId());
                        if (status == PaymentStatus.CONFIRMED || status == PaymentStatus.AUTHORIZED) {
                            CommonSdkHandler.INSTANCE.obtainMessage(CommonSdkHandler.SUCCESS).sendToTarget();
                        } else {
                            CommonSdkHandler.INSTANCE.obtainMessage(CommonSdkHandler.EXCEPTION, new AcquiringSdkException(new IllegalStateException("PaymentState = " + status))).sendToTarget();
                        }
                    } else {
                        GetAddCardStateResponse response = sdk.getAddCardState(threeDsData.getRequestKey());
                        PaymentStatus status = response.getStatus();
                        if (status == PaymentStatus.COMPLETED) {
                            AttachCardFormHandler.INSTANCE.obtainMessage(AttachCardFormHandler.CARD_ID, response.getCardId()).sendToTarget();
                            CommonSdkHandler.INSTANCE.obtainMessage(CommonSdkHandler.SUCCESS).sendToTarget();
                        } else {
                            CommonSdkHandler.INSTANCE.obtainMessage(CommonSdkHandler.EXCEPTION, new AcquiringSdkException(new IllegalStateException("PaymentState = " + status))).sendToTarget();
                        }
                    }
                } catch (Exception e) {
                    CommonSdkHandler.INSTANCE.obtainMessage(CommonSdkHandler.EXCEPTION, e).sendToTarget();
                }
            }
        }).start();
    }

    private static String getTimeZoneOffset() {
        int offsetMills = TimeZone.getDefault().getRawOffset();
        String prefix = offsetMills >= 0 ? "-" : "+";
        return prefix + TimeUnit.MILLISECONDS.toMinutes(offsetMills);
    }
}
