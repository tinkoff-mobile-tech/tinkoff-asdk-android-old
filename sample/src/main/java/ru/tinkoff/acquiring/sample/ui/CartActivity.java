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

package ru.tinkoff.acquiring.sample.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Random;

import ru.tinkoff.acquiring.sample.Book;
import ru.tinkoff.acquiring.sample.BooksRegistry;
import ru.tinkoff.acquiring.sample.Cart;
import ru.tinkoff.acquiring.sample.R;
import ru.tinkoff.acquiring.sample.adapters.CartListAdapter;
import ru.tinkoff.acquiring.sdk.Money;

/**
 * @author Mikhail Artemyev
 */
public class CartActivity extends PayableActivity implements CartListAdapter.DeleteCartItemListener {

    public static void start(final Context context) {
        context.startActivity(new Intent(context, CartActivity.class));
    }

    private ListView listViewCartItems;
    private TextView textViewTotalPrice;
    private TextView buttonPay;

    private ArrayAdapter adapter;

    private Boolean cartEmpty;

    private Money totalPrice;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        refreshContentView();

        if (cartEmpty) {
            return;
        }

        listViewCartItems = (ListView) findViewById(R.id.lv_cart_items);
        textViewTotalPrice = (TextView) findViewById(R.id.tv_total_price);

        buttonPay = (TextView) findViewById(R.id.btn_pay);
        buttonPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String title = Cart.getInstance().size() > 1 ?
                        getString(R.string.pay_form_title_multiple) :
                        getString(R.string.pay_form_title_single);
                initPayment(totalPrice, getOrderId(), title, getBooksAnnounce());
            }
        });
    }

    private void refreshContentView() {
        final Boolean cartEmptyIsEmptyNow = Cart.getInstance().size() == 0;
        if (cartEmptyIsEmptyNow.equals(cartEmpty)) {
            return;
        }

        cartEmpty = cartEmptyIsEmptyNow;
        final int layoutId = cartEmpty ? R.layout.activity_cart_empty : R.layout.activity_cart;
        setContentView(layoutId);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (cartEmpty) {
            return;
        }

        BooksRegistry registry = new BooksRegistry();
        adapter = new CartListAdapter(this, this, Cart.getInstance(), registry);
        listViewCartItems.setAdapter(adapter);

        updateBottomBar();
    }

    private void updateBottomBar() {
        long priceCoins = 0L;
        for (final Cart.CartEntry entry : Cart.getInstance()) {
            priceCoins += entry.getPrice().getCoins();
        }

        totalPrice = Money.ofCoins(priceCoins);

        final String stringTotalPrice = getString(R.string.book_price_total, totalPrice);
        textViewTotalPrice.setText(stringTotalPrice);
        buttonPay.setEnabled(totalPrice.getCoins() > 0L);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cart_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem clearItem = menu.findItem(R.id.menu_action_clear);
        clearItem.setVisible(!cartEmpty);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_action_clear) {
            Cart.getInstance().clear();
            refreshContentView();
            invalidateOptionsMenu();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSuccess(long paymentId) {
        super.onSuccess(paymentId);
        Cart.getInstance().clear();
        refreshContentView();
    }

    private String getOrderId() {
        return String.valueOf(Math.abs(new Random().nextInt()));
    }

    @Override
    public void onDeleteItemPressed(Cart.CartEntry cartEntry) {
        Cart.getInstance().remove(cartEntry);
        adapter.notifyDataSetChanged();
        updateBottomBar();
        refreshContentView();
        invalidateOptionsMenu();
    }


    private String getBooksAnnounce() {
        final StringBuilder result = new StringBuilder();
        BooksRegistry booksRegistry = new BooksRegistry();
        for (final Cart.CartEntry entry : Cart.getInstance()) {
            Book book = booksRegistry.getBook(this, entry.getBookId());
            result.append(book.getAnnounce());
            result.append(",\n");
        }

        result.setLength(result.length() - 2);
        return result.toString();
    }
}
