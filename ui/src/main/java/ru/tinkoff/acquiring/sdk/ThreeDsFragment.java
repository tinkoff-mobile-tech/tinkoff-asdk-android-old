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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.net.URLEncoder;

/**
 * @author a.shishkin1
 */
public class ThreeDsFragment extends Fragment {

    public static final String EXTRA_3DS = "extra_3ds";

    private static final String CANCEL_ACTION = "cancel.do";
    private static final String SUBMIT_3DS_AUTHORIZATION = "/Submit3DSAuthorization";

    private WebView wvThreeDs;
    private ThreeDsData data;
    private AcquiringSdk sdk;
    private String termUrl;

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
        sdk = ((PayFormActivity) getActivity()).getSdk();
        String url = data.getAcsUrl();
        termUrl = sdk.getUrl(SUBMIT_3DS_AUTHORIZATION) + SUBMIT_3DS_AUTHORIZATION;
        try {
            String params = new StringBuilder()
                    .append("PaReq=").append(URLEncoder.encode(data.getPaReq(), "UTF-8"))
                    .append("&MD=").append(URLEncoder.encode(data.getMd(), "UTF-8"))
                    .append("&TermUrl=").append(URLEncoder.encode(termUrl, "UTF-8"))
                    .toString();
            wvThreeDs.postUrl(url, params.getBytes());
        } catch (Exception e) {
            throw new AcquiringSdkException(e);
        }
    }

    private class ThisWebViewClient extends WebViewClient {



        boolean canceled = false;

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (url.contains(CANCEL_ACTION)) {
                canceled = true;
                Activity activity = (Activity)view.getContext();
                activity.setResult(Activity.RESULT_CANCELED);
                activity.finish();
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

    private static void requestState(final AcquiringSdk acquiringSdk, final ThreeDsData threeDsData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PaymentStatus status = acquiringSdk.getState(threeDsData.getPaymentId());
                    if (status == PaymentStatus.CONFIRMED || status == PaymentStatus.AUTHORIZED) {
                        PayFormActivity.handler.obtainMessage(SdkHandler.SUCCESS).sendToTarget();
                    } else {
                        PayFormActivity.handler.obtainMessage(SdkHandler.EXCEPTION, new AcquiringSdkException(new IllegalStateException("PaymentState = " + status))).sendToTarget();
                    }
                } catch (final Exception e) {
                    PayFormActivity.handler.obtainMessage(SdkHandler.EXCEPTION, e).sendToTarget();
                }
            }
        }).start();
    }


}
