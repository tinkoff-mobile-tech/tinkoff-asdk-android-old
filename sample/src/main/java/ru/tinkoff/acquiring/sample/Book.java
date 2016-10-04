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

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;

import java.util.Locale;

import ru.tinkoff.acquiring.sdk.Money;

/**
 * @author Mikhail Artemyev
 */
public class Book implements Parcelable {

    private int id;
    @DrawableRes
    private int coverDrawableId;
    private String title;
    private String author;
    private String annotation;
    private int year;
    private Money price;

    public Book(int id) {
        this.id = id;
    }

    public Book(Book source) {
        this.coverDrawableId = source.coverDrawableId;
        this.title = source.title;
        this.author = source.author;
        this.annotation = source.annotation;
        this.year = source.year;
        this.price = source.price;
        this.id = source.id;
    }

    protected Book(Parcel in) {
        this.coverDrawableId = in.readInt();
        this.title = in.readString();
        this.author = in.readString();
        this.annotation = in.readString();
        this.year = in.readInt();
        this.price = (Money) in.readSerializable();
        this.id = in.readInt();
    }

    public int getId() {
        return this.id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.coverDrawableId);
        dest.writeString(this.title);
        dest.writeString(this.author);
        dest.writeString(this.annotation);
        dest.writeInt(this.year);
        dest.writeSerializable(this.price);
        dest.writeSerializable(this.id);
    }

    @DrawableRes
    public int getCoverDrawableId() {
        return coverDrawableId;
    }

    public void setCoverDrawableId(@DrawableRes int coverDrawableId) {
        this.coverDrawableId = coverDrawableId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Money getPrice() {
        return price;
    }

    public void setPrice(Money price) {
        this.price = price;
    }

    public String getAnnounce() {
        Locale locale = Locale.getDefault();
        return String.format(locale, "\"%s\" (%s, %d)", title, author, year);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Book)) {
            return false;
        }

        Book book = (Book) o;

        return id == book.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {
        public Book createFromParcel(Parcel source) {
            return new Book(source);
        }

        public Book[] newArray(int size) {
            return new Book[size];
        }
    };
}
