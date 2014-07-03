package me.ronshapiro.lucas.uses.venmo;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

public abstract class SilencingReselectingTextWatcher implements TextWatcher {

    private TextView mTextView;

    public SilencingReselectingTextWatcher(TextView textView) {
        mTextView = textView;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        // don't bother with this TextWatcher recursing by calling mTextView.setText();
        // remove the watcher and then add it back at the end.
        mTextView.removeTextChangedListener(this);
        int selectionStart = mTextView.getSelectionStart();
        int selectionEnd = mTextView.getSelectionEnd();

        onAfterTextChanged(s);
        if (mTextView instanceof EditText) {
            EditText field = (EditText) mTextView;
            field.setSelection(selectionStart, selectionEnd);
        }

        mTextView.addTextChangedListener(this);
    }

    public TextView getTextView() {
        return mTextView;
    }

    /**
     * Isolated version of {@link #afterTextChanged} - gives the {@link android.text.TextWatcher}
     * the ability to modify the text of the {@link android.widget.TextView}.
     *
     * @param s see {@link #afterTextChanged(android.text.Editable)
     */
    public abstract void onAfterTextChanged(Editable s);
}
