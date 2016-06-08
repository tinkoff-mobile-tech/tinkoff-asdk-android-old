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
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import ru.tinkoff.acquiring.sample.Book;
import ru.tinkoff.acquiring.sample.R;

/**
 * @author Mikhail Artemyev
 */
public class BooksListAdapter extends BaseBooksListAdapter {

    public interface BookDetailsClickListener {
        void onBookDetailsClicked(Book book);
    }

    private BookDetailsClickListener listener;

    public BooksListAdapter(Context context, ArrayList<Book> objects, BookDetailsClickListener listener) {
        super(context, R.layout.list_item_book, objects);
        this.listener = listener;
    }

    @Override
    protected BaseBooksListAdapter.ViewHolder createViewHolder(View rootView) {
        return new ViewHolder(rootView);
    }

    private class ViewHolder extends BaseBooksListAdapter.ViewHolder implements View.OnClickListener {
        private TextView textViewAuthor;
        private TextView textViewAnnotation;

        ViewHolder(final View rootView) {
            super(rootView);

            textViewAuthor = (TextView) rootView.findViewById(R.id.tv_book_author_year);
            textViewAnnotation = (TextView) rootView.findViewById(R.id.tv_book_annotation);

            final TextView textViewDetails = (TextView) rootView.findViewById(R.id.tv_book_details);
            textViewDetails.setOnClickListener(this);
        }

        protected void fillWith(final Book book) {
            super.fillWith(book);

            textViewAuthor.setText(String.format(Locale.getDefault(), "%s, %d", book.getAuthor(), book.getYear()));
            textViewAnnotation.setText(book.getAnnotation());
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onBookDetailsClicked(getBook());
            }
        }
    }
}
