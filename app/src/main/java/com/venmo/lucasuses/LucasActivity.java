package com.venmo.lucasuses;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.EditText;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * {@link Activity}s are Android's concept of a "Screen". They are the Controller in the MVC pattern
 * - they manage user input and make the communications between data models and views. For those
 * curious about having Controller components that are not a full screen, check out Fragments
 *
 * @see <a href="http://developer.android.com/guide/components/fragments.html">Fragments</a>
 */
public class LucasActivity extends ActionBarActivity {

    private static final String TAG = LucasActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lucas_meme);

        // Android API-13 and below don't have android:textAllCaps="true"
        // Let's implement it below
        final EditText bannerText = findViewExtended(R.id.meme_banner_text);
        bannerText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // we're going to need to reset the selection after we modify the text, otherwise
                // after each character typed it will be reset to the beginning of the EditText
                int selectionStart = bannerText.getSelectionStart();
                int selectionEnd = bannerText.getSelectionEnd();
                String beforeChanges = s.toString();
                String allCaps = beforeChanges.toUpperCase();
                if (!allCaps.equals(beforeChanges)) { // Protect against StackOverflow
                    bannerText.setText(allCaps);
                    bannerText.setSelection(selectionStart, selectionEnd);
                }
            }
        });

        // put the TextWatcher into effect even on the android:text attribute in XML
        // For the more curious: what's another way to call this?
        bannerText.setText(bannerText.getText());

        EditText bodyText = findViewExtended(R.id.meme_body_text);
        bodyText.addTextChangedListener(new SilencingReselectingTextWatcher(bodyText) {
            @Override
            public void onAfterTextChanged(Editable s) {
                List<String> lines = Lists.newLinkedList(Splitter.on('\n').split(s));
                SpannableStringBuilder builder = new SpannableStringBuilder();
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
                    // let's insert some spans in the line
                    Spannable span = new SpannableString(line);
                    int color = Color.LTGRAY;
                    if (CharMatcher.JAVA_UPPER_CASE.matchesAllOf(line)) {
                        color = Color.BLACK;
                    }
                    span.setSpan(new ForegroundColorSpan(color), 0, line.length(),
                            Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    builder.append(span);

                    // join back '\n'; We can't use Guava's `Joiner` since it returns a String,
                    // but we need a CharSequence (String implements CharSequence; so does
                    // Spannable) so that the spans are transferred to the EditText
                    if (i != lines.size() - 1) {
                        builder.append('\n');
                    }
                }
                getTextView().setText(builder);
            }
        });
        bodyText.setText(bodyText.getText());
    }

    /**
     * Use generics to our advantage! wraps {@link #findViewById(int)} and automatically casts the
     * result to whatever view we are assigning the returned value. Super helpful!
     */
    @SuppressWarnings("unchecked")
    private <E extends View> E findViewExtended(int resourceId) {
        return (E) super.findViewById(resourceId);
    }
}




