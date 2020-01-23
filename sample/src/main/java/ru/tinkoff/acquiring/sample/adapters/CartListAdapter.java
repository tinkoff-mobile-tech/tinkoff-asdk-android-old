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
import androidx.core.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ru.tinkoff.acquiring.sample.Book;
import ru.tinkoff.acquiring.sample.BooksRegistry;
import ru.tinkoff.acquiring.sample.Cart;
import ru.tinkoff.acquiring.sample.R;

/**
 * @author Mikhail Artemyev
 */
public class CartListAdapter extends ArrayAdapter<Cart.CartEntry> {

    public interface DeleteCartItemListener {
        void onDeleteItemPressed(Cart.CartEntry cartEntry);
    }

    private DeleteCartItemListener listener;
    private LayoutInflater inflater;
    private String countFormat;
    private BooksRegistry booksRegistry;

    public CartListAdapter(Context context, DeleteCartItemListener listener, List<Cart.CartEntry> objects, BooksRegistry registry) {
        super(context, R.layout.list_item_cart, objects);
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
        this.booksRegistry = registry;
        countFormat = context.getString(R.string.cart_list_item_count_format);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_cart, parent, false);
            convertView.setTag(new ViewHolder(convertView));
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        Cart.CartEntry entry = getItem(position);
        Book book = booksRegistry.getBook(getContext(), entry.getBookId());
        holder.fillWith(entry, book);
        return convertView;
    }

    private class ViewHolder implements View.OnClickListener {

        private TextView textViewPrice;
        private TextView textViewTitle;
        private ImageView imageViewCover;

        private Cart.CartEntry cartEntry;

        ViewHolder(View view) {
            imageViewCover = (ImageView) view.findViewById(R.id.iv_book_cover);
            textViewTitle = (TextView) view.findViewById(R.id.tv_book_title);
            textViewPrice = (TextView) view.findViewById(R.id.tv_book_price);
            TextView textViewDelete = (TextView) view.findViewById(R.id.tv_delete);
            textViewDelete.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onDeleteItemPressed(cartEntry);
        }

        private void fillWith(Cart.CartEntry cartEntry, Book book) {
            this.cartEntry = cartEntry;
            imageViewCover.setImageResource(book.getCoverDrawableId());
            textViewTitle.setText(book.getTitle());
            int count = cartEntry.getCount();
            if (count > 1) {
                CharSequence countPart = String.format(countFormat, count);
                CharSequence pricePart = book.getShoppingTitle();
                SpannableStringBuilder label = new SpannableStringBuilder(countPart);
                label.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.common_gray)), 0, countPart.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                label.append(pricePart);
                textViewPrice.setText(label);
            } else {
                textViewPrice.setText(book.getShoppingTitle());
            }
        }
    }
}
