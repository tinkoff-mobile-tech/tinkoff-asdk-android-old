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
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ru.tinkoff.acquiring.sample.Book;
import ru.tinkoff.acquiring.sample.R;

/**
 * @author Mikhail Artemyev
 */
public class BaseBooksListAdapter<T extends Book> extends ArrayAdapter<T> {

    private final LayoutInflater inflater;
    private final int itemLayout;

    public BaseBooksListAdapter(Context context, @LayoutRes int itemLayout, List<T> objects) {
        super(context, itemLayout, objects);
        this.inflater = LayoutInflater.from(context);
        this.itemLayout = itemLayout;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rootView = convertView;
        if (rootView == null) {
            rootView = inflater.inflate(itemLayout, parent, false);
            rootView.setTag(createViewHolder(rootView));
        }

        final ViewHolder viewHolder = (ViewHolder) rootView.getTag();
        viewHolder.fillWith(getItem(position));

        return rootView;
    }

    protected ViewHolder createViewHolder(View rootView) {
        return new ViewHolder(rootView);
    }

    protected class ViewHolder {

        private ImageView imageViewCover;
        private TextView textViewTitle;
        private TextView textViewPrice;

        private Book book;

        ViewHolder(final View rootView) {
            if (rootView == null) {
                throw new IllegalArgumentException("rootView is null");
            }

            imageViewCover = (ImageView) rootView.findViewById(R.id.iv_book_cover);
            textViewTitle = (TextView) rootView.findViewById(R.id.tv_book_title);
            textViewPrice = (TextView) rootView.findViewById(R.id.tv_book_price);
        }

        protected void fillWith(final Book book) {
            this.book = book;

            imageViewCover.setImageResource(book.getCoverDrawableId());
            textViewTitle.setText(book.getTitle());

            // formatPrice
            final CharSequence priceText = createPriceString(book);
            textViewPrice.setText(priceText);
        }

        protected CharSequence createPriceString(Book book) {
            return getContext().getString(R.string.book_price, book.getPrice());
        }

        protected Book getBook() {
            return book;
        }
    }
}
