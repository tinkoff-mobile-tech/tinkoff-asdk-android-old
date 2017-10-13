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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;

import ru.tinkoff.acquiring.sample.Book;
import ru.tinkoff.acquiring.sample.BooksRegistry;
import ru.tinkoff.acquiring.sample.MerchantParams;
import ru.tinkoff.acquiring.sample.R;
import ru.tinkoff.acquiring.sample.SettingsSdkManager;
import ru.tinkoff.acquiring.sample.adapters.BooksListAdapter;
import ru.tinkoff.acquiring.sdk.AttachCardFormActivity;

public class MainActivity extends AppCompatActivity implements
        BooksListAdapter.BookDetailsClickListener {

    private static final int ATTACH_CARD_REQUEST_CODE = 11;

    private ListView listViewBooks;
    private BooksListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        listViewBooks = (ListView) findViewById(R.id.lv_books);

        initViews(getBooks());
    }

    private void initViews(ArrayList<Book> books) {
        adapter = new BooksListAdapter(this, books, this);
        listViewBooks.setAdapter(adapter);
        setContentView(listViewBooks);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_cart:
                CartActivity.start(this);
                return true;
            case R.id.menu_action_attach_card:
                SettingsSdkManager settings = new SettingsSdkManager(this);
                String terminalId = settings.getTerminalId();
                AttachCardFormActivity
                        .init(terminalId, MerchantParams.PASSWORD, MerchantParams.PUBLIC_KEY)
                        .prepare(settings.resolveCustomerKey(terminalId), settings.isCustomKeyboardEnabled())
                        .setTheme(settings.resolveStyle())
                        .startActivityForResult(this, ATTACH_CARD_REQUEST_CODE);
                return true;
            case R.id.menu_action_about:
                AboutActivity.start(this);
                return true;
            case R.id.menu_action_settings:
                SettingsActivity.start(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBookDetailsClicked(Book book) {
        DetailsActivity.start(this, book);
    }

    private ArrayList<Book> getBooks() {
        final BooksRegistry booksRegistry = new BooksRegistry();
        return booksRegistry.getBooks(this);
    }
}
