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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author a.shishkin1
 */
public class CardListFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, ICardInterest {

    public static String EXTRA_CUSTOMER_KEY = "customer_key";

    private ListView lvCards;
    private CardsAdapter adapter;

    private String customerKey;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.acq_fragment_card_list, container, false);
        lvCards = (ListView) view.findViewById(R.id.lv_cards);
        adapter = new CardsAdapter(getActivity());
        lvCards.setAdapter(adapter);
        lvCards.setOnItemClickListener(this);
        lvCards.setOnItemLongClickListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        customerKey = getArguments().getString(EXTRA_CUSTOMER_KEY);
        CardManager cardManager = ((PayFormActivity)getActivity()).getCardManager();
        ((PayFormActivity) getActivity()).requestCards(customerKey, cardManager);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Item item = adapter.getItem(position);
        PayFormActivity activity = ((PayFormActivity) getActivity());
        Card card = (Card) item.obj;
        activity.getFragmentsCommunicator().setPendingResult(PayFormActivity.RESULT_CODE_CLEAR_CARD, Bundle.EMPTY);
        activity.setSourceCard(card);
        activity.finishChooseCards();
    }


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Item item = adapter.getItem(position);
        Card card = (Card) item.obj;
        if (card == null) {
            return false;
        }
        ((AppCompatActivity) getActivity()).startSupportActionMode(new CardLongPressCallback(card));
        return true;
    }

    @Override
    public void onCardReady() {
        Card[] cards = ((PayFormActivity) getActivity()).getCards();
        adapter.setCards(cards);
    }


    private static class CardsAdapter extends BaseAdapter {

        private List<Item> items = new ArrayList<>();
        private Context context;
        private CardLogoCache cardLogoCache;

        public CardsAdapter(Activity context) {
            this.context = context;
            cardLogoCache = new ThemeCardLogoCache(context);
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return getItem(position).type != Item.EMPTY;
        }

        public void setCards(Card[] cards) {
            List<Item> items = new ArrayList<>();
            if (cards != null && cards.length > 0) {
                for (Card card : cards) {
                    items.add(new Item(card));
                }
                items.add(new Item());
            }
            items.add(new Item(Item.NEW_CARD));
            this.items = items;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Item getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int type = getItemViewType(position);
            if (type == Item.EMPTY) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(context).inflate(R.layout.acq_item_divider, parent, false);
                }
                return convertView;
            } else if (type == Item.CARD) {
                Item item = getItem(position);
                Card card = (Card) item.obj;
                if (convertView == null) {
                    convertView = LayoutInflater.from(context).inflate(R.layout.acq_item_card, parent, false);
                }
                String name = card.getPan();
                ((ImageView) (convertView.findViewById(R.id.iv_icon))).setImageBitmap(cardLogoCache.getLogoByNumber(context, name));
                ((TextView) (convertView.findViewById(R.id.tv_card_name))).setText(name);
                convertView.findViewById(R.id.iv_daw).setVisibility(((PayFormActivity)convertView.getContext()).getSourceCard() == card ? View.VISIBLE : View.GONE);
                return convertView;
            } else if (type == Item.NEW_CARD) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(context).inflate(R.layout.acq_item_new_card, parent, false);
                }
                return convertView;
            }
            throw new IllegalStateException("no views for type " + type);
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).type;
        }

        @Override
        public int getViewTypeCount() {
            return Item.TYPE_COUNT;
        }
    }


    private static class Item {

        public static final int TYPE_COUNT = 3;

        public static final int EMPTY = 0;
        public static final int CARD = 1;
        public static final int NEW_CARD = 2;

        private int type;
        private Object obj;

        public Item() {
            type = EMPTY;
        }

        public Item(Card card) {
            obj = card;
            type = CARD;
        }

        public Item(int type) {
            this.type = type;
        }
    }


    private static void deleteCard(final AcquiringSdk sdk, final Card card, final String customerKey, final String cardNotDeletedErrMsg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean isDeleted = sdk.removeCard(customerKey, card.getCardId());
                    if (isDeleted) {
                        PayFormActivity.handler.obtainMessage(SdkHandler.DELETE_CARD, card).sendToTarget();
                    } else {
                        throw new AcquiringSdkException(new RuntimeException(cardNotDeletedErrMsg));
                    }
                } catch (Exception e) {
                    PayFormActivity.handler.obtainMessage(SdkHandler.SHOW_ERROR_DIALOG, e).sendToTarget();
                }
            }
        }).start();
    }

    private class CardLongPressCallback implements ActionMode.Callback {

        private Card target;

        public CardLongPressCallback(Card target) {
            this.target = target;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_delete_card, menu);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.action_delete) {
                AcquiringSdk sdk = ((PayFormActivity) getActivity()).getSdk();
                deleteCard(sdk, target, customerKey, getString(R.string.acq_cant_delete_card_message));
                mode.finish();
            }
            return false;
        }


    }

}
