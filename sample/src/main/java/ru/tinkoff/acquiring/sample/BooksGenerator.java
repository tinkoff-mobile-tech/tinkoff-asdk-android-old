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

import java.util.ArrayList;

import ru.tinkoff.acquiring.sdk.Money;

/**
 * @author Mikhail Artemyev
 */
public class BooksGenerator {

    private static final int SIZE = 6;

    private static final int[] YEARS = {2016, 2004, 2015, 2014, 2016, 2015};
    private static final Money[] PRICES = {
            Money.ofRubles(2000L), Money.ofRubles(999.99d), Money.ofRubles(1699L),
            Money.ofRubles(4500L), Money.ofRubles(2999.99d), Money.ofRubles(600.99d)
    };

    public Book getBook(Context context, int id) {
        for (Book book : getBooks(context)) {
            if (book.getId() == id) {
                return book;
            }
        }
        throw new RuntimeException("Unknown book id " + id);
    }

    public ArrayList<Book> getBooks(Context context) {

        final String[] authors = context.getResources().getStringArray(R.array.book_authors);
        final String[] titles = context.getResources().getStringArray(R.array.book_titles);
        final String[] descriptions = context.getResources().getStringArray(R.array.book_descriptions);

        ArrayList<Book> booksList = new ArrayList<>(SIZE);
        for (int i = 0; i < SIZE; i++) {

            final Book book = new Book(i);

            book.setAuthor(authors[i]);
            book.setTitle(titles[i]);
            book.setAnnotation(descriptions[i]);
            book.setYear(YEARS[i]);
            book.setPrice(PRICES[i]);
            book.setCoverDrawableId(R.drawable.cover_1);

            booksList.add(book);
        }

        return booksList;
    }

}
