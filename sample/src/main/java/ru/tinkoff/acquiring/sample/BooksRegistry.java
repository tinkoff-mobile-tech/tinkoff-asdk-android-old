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

package ru.tinkoff.acquiring.sample;

import android.content.Context;
import android.content.res.Resources;

import java.util.ArrayList;

import ru.tinkoff.acquiring.sdk.Money;

/**
 * @author Mikhail Artemyev
 */
public class BooksRegistry {

    public Book getBook(Context context, int id) {
        for (Book book : getBooks(context)) {
            if (book.getId() == id) {
                return book;
            }
        }
        return null;
    }

    public ArrayList<Book> getBooks(Context context) {

        Resources resources = context.getResources();
        String[] authors = resources.getStringArray(R.array.book_authors);
        String[] titles = resources.getStringArray(R.array.book_titles);
        String[] descriptions = resources.getStringArray(R.array.book_descriptions);
        String[] years = resources.getStringArray(R.array.book_years);
        String[] prices = resources.getStringArray(R.array.book_prices);

        int size = resources.getInteger(R.integer.books_count);

        ArrayList<Book> booksList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {

            final Book book = new Book(i);

            book.setAuthor(authors[i]);
            book.setTitle(titles[i]);
            book.setAnnotation(descriptions[i]);
            book.setYear(years[i]);
            String priceString = prices[i];
            Money price = Money.ofRubles(Double.valueOf(priceString));
            book.setPrice(price);
            book.setCoverDrawableId(R.drawable.cover_1);
            booksList.add(book);
        }

        return booksList;
    }

}
