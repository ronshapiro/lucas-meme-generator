package com.venmo.lucasuses;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

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
    }

    /**
     * Use generics to our advantage! wraps {@link #findViewById(int)} and automatically casts the
     * result to whatever view we are assigning the returned value. Super helpful!
     */
    @SuppressWarnings("unchecked")
    private <E extends View> E findViewExtended(int resourceId){
        return (E) super.findViewById(resourceId);
    }
}




