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
    private ActionMode actionMode;

    public static CardListFragment newInstance(String customerKey, boolean chargeMode) {
        Bundle args = new Bundle();
        args.putString(EXTRA_CUSTOMER_KEY, customerKey);
        args.putBoolean(PayFormActivity.EXTRA_CHARGE_MODE, chargeMode);
        CardListFragment fragment = new CardListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.acq_fragment_card_list, container, false);
        lvCards = (ListView) view.findViewById(R.id.lv_cards);
        adapter = new CardsAdapter(getActivity(), getArguments().getBoolean(PayFormActivity.EXTRA_CHARGE_MODE, false));
        lvCards.setAdapter(adapter);
        lvCards.setOnItemClickListener(this);
        lvCards.setOnItemLongClickListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        customerKey = getArguments().getString(EXTRA_CUSTOMER_KEY);
        CardManager cardManager = ((PayFormActivity) getActivity()).getCardManager();
        ((PayFormActivity) getActivity()).requestCards(customerKey, cardManager);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Item item = adapter.getItem(position);
        PayFormActivity activity = ((PayFormActivity) getActivity());
        Card card = (Card) item.obj;

        if (actionMode != null && card != null) {
            actionMode.invalidate();
            adapter.setSelectedItemPosition(position);
            view.setSelected(true);
        } else if (actionMode != null) {
            actionMode.finish();
        } else {
            activity.getFragmentsCommunicator().setPendingResult(PayFormActivity.RESULT_CODE_CLEAR_CARD, Bundle.EMPTY);
            activity.setSourceCard(card);
            activity.finishChooseCards();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Item item = adapter.getItem(position);
        Card card = (Card) item.obj;
        if (card == null) {
            return false;
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        adapter.setSelectedItemPosition(position);
        view.setSelected(true);

        if (actionMode != null) {
            actionMode.invalidate();
        } else {
            actionMode = activity.startSupportActionMode(new CardLongPressCallback());
        }
        return true;
    }

    @Override
    public void onCardReady() {
        Card[] cards = ((PayFormActivity) getActivity()).getCards();
        adapter.setCards(cards);
    }


    private static class CardsAdapter extends BaseAdapter {

        private static final int NOT_SET = -1;

        private List<Item> items = new ArrayList<>();
        private Activity context;
        private CardLogoCache cardLogoCache;
        private final boolean chargeMode;
        private int selectedItemPosition = NOT_SET;

        public CardsAdapter(Activity context, boolean chargeMode) {
            this.context = context;
            this.chargeMode = chargeMode;
            this.cardLogoCache = new ThemeCardLogoCache(context);
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return getItem(position).type != Item.EMPTY;
        }

        public void setSelectedItemPosition(int position) {
            selectedItemPosition = position;
        }

        public void setCards(Card[] cards) {
            List<Item> items = new ArrayList<>();
            if (cards != null && cards.length > 0) {
                for (Card card : cards) {
                    items.add(new Item(card));
                }
                items.add(new Item());
            }
            if (!chargeMode) {
                items.add(new Item(Item.NEW_CARD));
            }
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
                convertView.findViewById(R.id.iv_daw).setVisibility(((PayFormActivity) context).getSourceCard() == card ? View.VISIBLE : View.GONE);
                boolean isItemSelected = (selectedItemPosition != NOT_SET && position == selectedItemPosition);
                convertView.setSelected(isItemSelected);
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

        Card getSelectedItem() {
            if (selectedItemPosition < 0 || selectedItemPosition > items.size()) {
                return null;
            }

            if (getItemViewType(selectedItemPosition) == Item.CARD) {
                return (Card) getItem(selectedItemPosition).obj;
            } else {
                return null;
            }
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

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            adapter.setSelectedItemPosition(CardsAdapter.NOT_SET);
            adapter.notifyDataSetChanged();
            actionMode = null;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_delete_card, menu);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() != R.id.action_delete) {
                return false;
            }

            Card target = adapter.getSelectedItem();
            AcquiringSdk sdk = ((PayFormActivity) getActivity()).getSdk();
            deleteCard(sdk, target, customerKey, getString(R.string.acq_cant_delete_card_message));
            mode.finish();
            return true;
        }
    }
}
