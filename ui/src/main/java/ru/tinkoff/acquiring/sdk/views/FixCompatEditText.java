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

package ru.tinkoff.acquiring.sdk.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * @author a.shishkin1
 *         <p/>
 *         Поскольку поле ввода номера карты должно выглядеть как все поля ввода на экране
 *         и не имеет Compat реализации с поддержкой TintableBackgroundView в sdk используются
 *         обычные EditText, а не AppCompatEditText. Поскольку андроид во время inflate подменяет
 *         описанные в layout файлах EditText на compat, для верстки используется данная
 *         реализация EditText.
 */
public class FixCompatEditText extends EditText {

    public FixCompatEditText(Context context) {
        super(context);
    }

    public FixCompatEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FixCompatEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

}

