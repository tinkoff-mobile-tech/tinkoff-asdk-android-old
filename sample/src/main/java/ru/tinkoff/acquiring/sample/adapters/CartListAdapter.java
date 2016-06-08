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

package ru.tinkoff.acquiring.sample.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import ru.tinkoff.acquiring.sample.Book;
import ru.tinkoff.acquiring.sample.Cart;
import ru.tinkoff.acquiring.sample.R;

/**
 * @author Mikhail Artemyev
 */
public class CartListAdapter extends BaseBooksListAdapter {

    public interface DeleteCartItemListener {
        void onDeleteItemPressed(Book book);
    }

    private final DeleteCartItemListener listener;

    public CartListAdapter(Context context, DeleteCartItemListener listener, List<Cart.CartEntry> objects) {
        super(context, R.layout.list_item_cart, objects);
        this.listener = listener;
    }

    @Override
    protected BaseBooksListAdapter.ViewHolder createViewHolder(View rootView) {
        return new ViewHolder(rootView);
    }

    private class ViewHolder extends BaseBooksListAdapter.ViewHolder
            implements View.OnClickListener {

        ViewHolder(View rootView) {
            super(rootView);
            final TextView textViewDelete = (TextView) rootView.findViewById(R.id.tv_delete);
            textViewDelete.setOnClickListener(this);
        }

        @Override
        protected CharSequence createPriceString(Book book) {
            Cart.CartEntry thisBook = ((Cart.CartEntry) book);
            int count = thisBook.getCount();
            if (count > 1) {
                CharSequence countPart = String.format(Locale.getDefault(), "%d шт. ", count);
                CharSequence pricePart = super.createPriceString(book);
                SpannableStringBuilder result = new SpannableStringBuilder(countPart);
                result.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.common_gray)), 0, countPart.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                result.append(pricePart);
                return result;
            } else {
                return super.createPriceString(book);
            }
        }

        @Override
        public void onClick(View v) {
            listener.onDeleteItemPressed(getBook());
        }

    }
}
