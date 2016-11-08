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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * @author Mikhail Artemyev
 */
class DialogsManager {

    private final Context context;
    private ProgressDialog progressDialog;
    private AlertDialog messageDialog;

    DialogsManager(Context context) {
        this.context = context;
    }

    void showErrorDialog(final String title,
                         final String message) {
        showErrorDialog(title, message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    void showErrorDialog(final String title,
                         final String message,
                         final DialogInterface.OnClickListener onClickListener) {
        dismissDialogs();

        messageDialog = new AlertDialog.Builder(context)
                .setMessage(message)
                .setTitle(title)
                .setCancelable(false)
                .setNeutralButton(R.string.acq_dialog_dismiss_btn, onClickListener)
                .create();

        messageDialog.show();
    }

    void showProgressDialog(final String message) {
        dismissDialogs();

        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    void hideProgressDialog() {
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }

    void dismissDialogs() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        if (messageDialog != null) {
            messageDialog.dismiss();
        }
    }

}
