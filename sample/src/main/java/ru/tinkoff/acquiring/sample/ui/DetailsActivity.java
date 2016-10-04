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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Random;

import ru.tinkoff.acquiring.sample.Book;
import ru.tinkoff.acquiring.sample.BooksGenerator;
import ru.tinkoff.acquiring.sample.Cart;
import ru.tinkoff.acquiring.sample.R;

/**
 * @author Mikhail Artemyev
 */
public class DetailsActivity extends PayableActivity {

    private static final String EXTRA_BOOK = "book";

    public static void start(final Context context, final Book book) {
        final Intent intent = new Intent(context, DetailsActivity.class);
        intent.putExtra(EXTRA_BOOK, book.getId());
        context.startActivity(intent);
    }

    private ImageView imageViewCover;
    private TextView textViewTitle;
    private TextView textViewAuthor;
    private TextView textViewYear;
    private TextView textViewAnnotation;
    private TextView textViewPrice;
    private TextView buttonAddToCart;

    private Book book;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int bookId = getIntent().getIntExtra(EXTRA_BOOK, -1);
        if (bookId == -1) {
            throw new IllegalStateException("Book is not passed to the DetailsActivity. Start it with start() method");
        } else {
            BooksGenerator booksGenerator = new BooksGenerator();
            book = booksGenerator.getBook(this, bookId);
        }

        setContentView(R.layout.activity_details);

        imageViewCover = (ImageView) findViewById(R.id.iv_book_cover);
        textViewTitle = (TextView) findViewById(R.id.tv_book_title);
        textViewAuthor = (TextView) findViewById(R.id.tv_book_author);
        textViewYear = (TextView) findViewById(R.id.tv_book_year);
        textViewAnnotation = (TextView) findViewById(R.id.tv_book_annotation);
        textViewPrice = (TextView) findViewById(R.id.tv_book_price);

        buttonAddToCart = (TextView) findViewById(R.id.btn_add_to_cart);
        buttonAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cart.getInstance().add(new Cart.CartEntry(book.getId(), book.getPrice()));
                Toast.makeText(DetailsActivity.this, R.string.added_to_cart, Toast.LENGTH_SHORT).show();
            }
        });

        final TextView buttonBuy = (TextView) findViewById(R.id.btn_buy_now);
        buttonBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initPayment(
                        book.getPrice(),
                        getOrderId(),
                        getString(R.string.pay_form_title_single),
                        book.getAnnounce()
                );
            }
        });


        fillViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_action_cart) {
            CartActivity.start(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void fillViews() {
        imageViewCover.setImageResource(book.getCoverDrawableId());
        textViewTitle.setText(book.getTitle());
        textViewAuthor.setText(book.getAuthor());
        textViewYear.setText(String.valueOf(book.getYear()));
        textViewAnnotation.setText(book.getAnnotation());

        final String price = getString(R.string.book_price, book.getPrice());
        textViewPrice.setText(price);
    }

    private String getOrderId() {
        return String.valueOf(Math.abs(new Random().nextInt()));
    }
}
