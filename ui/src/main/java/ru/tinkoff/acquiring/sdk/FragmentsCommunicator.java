/*
 * Copyright � 2016 Tinkoff Bank
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

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

import java.util.HashMap;
import java.util.Map;

/**
 * @author a.shishkin1
 */


class FragmentsCommunicator  {

    private static final String BUNDLE_KEY = "FragmentsCommunicator.state";

    private PendingResults results;


    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            results = savedInstanceState.getParcelable(BUNDLE_KEY);
        } else {
            results = new PendingResults();
        }
    }

    public void onSavedInstanceState(Bundle outBundle) {
        outBundle.putParcelable(BUNDLE_KEY, results);
    }

    public void setPendingResult(int resultCode, Bundle data) {
        results.put(resultCode, data);
    }


    public Bundle getResult(int resultCode) {
        Bundle result = results.get(resultCode);
        results.delete(resultCode);
        return result;
    }




    interface IFragmentManagerExtender {
        FragmentsCommunicator getFragmentsCommunicator();
    }


    public static class PendingResults extends SparseArray<Bundle> implements Parcelable {


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(size());
            for(int i = 0; i < size(); i++) {
                int key = keyAt(i);
                dest.writeInt(key);
                dest.writeBundle(get(key));
            }
        }

        public static final Creator<PendingResults> CREATOR = new Creator<PendingResults>() {
            @Override
            public PendingResults createFromParcel(Parcel source) {
                PendingResults results = new PendingResults();
                int size = source.readInt();
                for (int i = 0; i < size; i++) {
                    results.put(source.readInt(), source.readBundle());
                }
                return results;
            }

            @Override
            public PendingResults[] newArray(int size) {
                return new PendingResults[size];
            }
        };

    }



}
