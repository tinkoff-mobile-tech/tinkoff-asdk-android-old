package ru.tinkoff.acquiring.sdk.localization;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import ru.tinkoff.acquiring.sdk.Language;
import ru.tinkoff.acquiring.sdk.R;
import ru.tinkoff.acquiring.sdk.TAcqIntentExtra;

/**
 * @author a.shishkin1
 */
public class AsdkLocalizations {

    private AsdkLocalizations() {
        throw new AssertionError();
    }

    public static  AsdkLocalization require(Fragment fragment) {
        return require(fragment.getActivity());
    }

    public static  AsdkLocalization require(Context context) {
        if (context instanceof HasAsdkLocalization) {
            return ((HasAsdkLocalization) context).getAsdkLocalization();
        } else {
            throw new ClassCastException(context.getClass().getCanonicalName() + " must implement HasAsdkLocalization" );
        }
    }

    static AsdkLocalization unsafeGet(Context context) {
        Context activityCandidate = context;
        while (!(activityCandidate instanceof Activity)) {
            if (activityCandidate == null) {
                throw new IllegalArgumentException("can't find activity");
            }

            if (activityCandidate instanceof ContextWrapper) {
                activityCandidate = ((ContextWrapper) activityCandidate).getBaseContext();
            }
        }

        Activity activity = (Activity) activityCandidate;

        int rawResource = activity.getIntent().getIntExtra(TAcqIntentExtra.EXTRA_LOCALIZATION_RAW_RESOURCE_ID, 0);
        if (rawResource != 0) {
            return createLocalization(activity, rawResource);
        }

        String filePath = activity.getIntent().getStringExtra(TAcqIntentExtra.EXTRA_LOCALIZATION_FILE_PATH);
        if (filePath != null) {
            return createLocalization(activity, new File(filePath));
        }

        String fullJsonSource = activity.getIntent().getStringExtra("json");
        if (fullJsonSource != null) {
            return createLocalization(activity, fullJsonSource);
        }

        int languageOrdinal = activity.getIntent().getIntExtra(TAcqIntentExtra.EXTRA_LANGUAGE, -1);
        if (languageOrdinal != -1) {
            try {
                return createLocalization(activity, Language.values()[languageOrdinal]);
            } catch (IndexOutOfBoundsException ex) {
                throw new IllegalArgumentException("Malformed language parameter " + languageOrdinal, ex);
            }
        } else {
           return createLocalization(activity, resolveLanguageByCurrentLocale(activity));
        }
    }

    public static Language resolveLanguageByCurrentLocale(Context context) {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = context.getResources().getConfiguration().locale;
        }
        if (locale.getLanguage().equals(new Locale("ru").getLanguage())) {
            return Language.RUSSIAN;
        } else {
            return Language.ENGLISH;
        }
    }

    private static AsdkLocalization createLocalization(Context context, Object src) {
        AsdkLocalization localization;
        if (src instanceof Integer) {
            localization = create(context, (Integer) src);
        } else if (src instanceof Language) {
            localization = create(context, (Language) src);
        } else if (src instanceof String) {
            localization = create((String) src);
        } else if (src instanceof File) {
            localization = create((File) src);
        } else {
            throw new IllegalArgumentException("unsupported localization source " + src);
        }
        ensureLocalization(localization);
        return localization;
    }

    private static AsdkLocalization create(
            Context context, int rawResourceId
    ) {
        try {
            return new RawResourceLocalizationParser(context.getApplicationContext(), new GsonLocalizationParser()).parse(rawResourceId);
        } catch (LocalizationParser.LocalizationParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static AsdkLocalization create(
            Context context, Language language
    ) {
        switch (language) {
            case ENGLISH: return create(context, R.raw.acq_localization_en);
            case RUSSIAN: return create(context, R.raw.acq_localization_ru);
            default: throw new IllegalArgumentException("unknown resource for language: " + language.name());
        }
    }

    private static AsdkLocalization create(File file) {
        try {
            return new FileLocalizationParser(new GsonLocalizationParser()).parse(file);
        } catch (LocalizationParser.LocalizationParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static AsdkLocalization create(String src) {
        try {
            return new GsonLocalizationParser().parse(src);
        } catch (LocalizationParser.LocalizationParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static void ensureLocalization(AsdkLocalization localization) {
        List<String> missing = new LinkedList<>();
        for (Field field : AsdkLocalization.class.getDeclaredFields()) {
            SerializedName serializedName = field.getAnnotation(SerializedName.class);

            if (serializedName == null) continue;

            try {
                if (field.get(localization) == null && field.getAnnotation(Optional.class) == null) {
                    missing.add(serializedName.value());
                }
            } catch (IllegalAccessException e) {
                // ignore
            }
        }

        if (missing.isEmpty()) return;

        throw new IllegalArgumentException("localization must contains: " + TextUtils.join(", ", missing));
    }

}
