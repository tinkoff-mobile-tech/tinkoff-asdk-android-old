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
import java.util.Random;

import ru.tinkoff.acquiring.sdk.Money;

/**
 * @author Mikhail Artemyev
 */
public class BooksGenerator {

    final Random random = new Random(System.currentTimeMillis());

    public ArrayList<Book> generateBooks(Context context, byte number) {
        if (number <= 0) {
            return null;
        }

        final String[] authors = context.getResources().getStringArray(R.array.book_authors);
        final String[] titles = context.getResources().getStringArray(R.array.book_titles);
        final String[] descriptions = context.getResources().getStringArray(R.array.book_descriptions);

        ArrayList<Book> booksList = new ArrayList<>(number);
        for (int i = 0; i < number; i++) {
            final Book book = new Book();

            book.setAuthor(next(authors));
            book.setTitle(next(titles));
            book.setAnnotation(next(descriptions));
            book.setYear(nextYear());
            book.setPrice(nextPrice());
            book.setCoverDrawableId(R.drawable.cover_1);

            booksList.add(book);
        }

        return booksList;
    }

    private String next(final String[] items) {
        final int index = Math.abs(random.nextInt()) % items.length;
        return items[index];
    }

    private int nextYear() {
        return Math.abs(random.nextInt()) % 100 + 1915;
    }

    private Money nextPrice() {
        final int rubles = Math.abs(random.nextInt()) % 5000 + 500;
        final double coins = Math.abs(random.nextDouble());
        return Money.ofRubles(rubles + coins);
    }

}
