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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ru.tinkoff.acquiring.sample.Book;
import ru.tinkoff.acquiring.sample.R;

/**
 * @author Mikhail Artemyev
 */
public class BooksListAdapter extends ArrayAdapter<Book> {

    public interface BookDetailsClickListener {
        void onBookDetailsClicked(Book book);
    }

    private BookDetailsClickListener listener;
    private LayoutInflater inflater;

    public BooksListAdapter(Context context, ArrayList<Book> objects, BookDetailsClickListener listener) {
        super(context, R.layout.list_item_book, objects);
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.list_item_book, parent, false);
            ViewHolder holder = new ViewHolder(view);
            view.setTag(holder);
        }

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.fillWith(getItem(position));

        return view;
    }

    private class ViewHolder implements View.OnClickListener {

        private ImageView imageViewCover;
        private TextView textViewTitle;
        private TextView textViewPrice;
        private TextView textViewAuthor;
        private TextView textViewAnnotation;

        private Book book;

        ViewHolder(View view) {
            textViewAuthor = (TextView) view.findViewById(R.id.tv_book_author_year);
            textViewAnnotation = (TextView) view.findViewById(R.id.tv_book_annotation);
            imageViewCover = (ImageView) view.findViewById(R.id.iv_book_cover);
            textViewTitle = (TextView) view.findViewById(R.id.tv_book_title);
            textViewPrice = (TextView) view.findViewById(R.id.tv_book_price);
            TextView textViewDetails = (TextView) view.findViewById(R.id.tv_book_details);
            textViewDetails.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onBookDetailsClicked(book);
            }
        }

        private void fillWith(final Book book) {
            this.book = book;
            imageViewCover.setImageResource(book.getCoverDrawableId());
            textViewTitle.setText(book.getTitle());
            CharSequence priceText = getContext().getString(R.string.book_price, book.getPrice());
            textViewPrice.setText(priceText);
            textViewAuthor.setText(book.getShoppingTitle());
            textViewAnnotation.setText(book.getAnnotation());
        }
    }
}
