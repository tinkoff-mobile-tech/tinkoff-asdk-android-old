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

import android.annotation.SuppressLint;

import java.util.ArrayList;

import ru.tinkoff.acquiring.sdk.Money;

/**
 * @author Mikhail Artemyev
 */
public class Cart extends ArrayList<Cart.CartEntry> {

    private static final Cart instance = new Cart();

    public static Cart getInstance() {
        return instance;
    }

    @SuppressLint("ParcelCreator")
    public static class CartEntry {

        private int bookId;

        private Money price;

        private int count;

        public CartEntry(int bookId, Money price) {
            this.bookId = bookId;
            this.count = 1;
        }

        public int getBookId() {
            return bookId;
        }

        private void increase() {
            count++;
        }

        public int getCount() {
            return count;
        }

        /**
         * @return true if entry not empty
         */
        private boolean decrease() {
            count--;
            return count != 0;
        }

        public Money getPrice() {
            return Money.ofCoins(price.getCoins() * count);
        }
    }

    @Override
    public boolean add(CartEntry cartEntry) {
        for (CartEntry entry : this) {
            if (entry.equals(cartEntry)) {
                entry.increase();
                return true;
            }
        }
        return super.add(cartEntry);
    }

    @Override
    public boolean remove(Object cartEntry) {
        CartEntry forDelete = null;
        for (CartEntry entry : this) {
            if (entry.equals(cartEntry)) {
                forDelete = entry;
                break;
            }
        }
        if (forDelete == null) {
            return false;
        }

        if (!forDelete.decrease()) {
            return super.remove(cartEntry);
        }
        return true;
    }


}
