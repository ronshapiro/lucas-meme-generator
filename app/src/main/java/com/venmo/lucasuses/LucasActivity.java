package com.venmo.lucasuses;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    private static final int REQUEST_FOR_PICTURE = 1000;
    private ImageView mMemeImage;

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

        mMemeImage = findViewExtended(R.id.meme_image);
        mMemeImage.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Intents are Android's mechanism of communicating between Framework components
                Intent getPictureIntent = new Intent();
                getPictureIntent.setType("image/*");
                getPictureIntent.setAction(Intent.ACTION_GET_CONTENT);

                Intent chooser = Intent.createChooser(getPictureIntent, "Select Picture");
                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{
                        new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                });
                startActivityForResult(chooser, REQUEST_FOR_PICTURE);
                return true; // tell Android we want to consume the click event
            }
        });
    }

    /**
     * Use generics to our advantage! wraps {@link #findViewById(int)} and automatically casts the
     * result to whatever view we are assigning the returned value. Super helpful!
     */
    @SuppressWarnings("unchecked")
    private <E extends View> E findViewExtended(int resourceId) {
        return (E) super.findViewById(resourceId);
    }

    /* Why does Activity provide onActivityResult() instead of a listener? */
    /* answer: the activity might be destoryed! */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_FOR_PICTURE:
                if (resultCode == Activity.RESULT_OK) {
                    InputStream inputStream;
                    try {
                        inputStream = getContentResolver().openInputStream(data.getData());
                    } catch (FileNotFoundException e) {
                        return; // this shouldn't happen - Android should give us a real file
                    }
                    Bitmap retrievedBitmap = BitmapFactory.decodeStream(inputStream);
                    // for those more curious: see Bitmap.createScaledBitmap
                    mMemeImage.setImageBitmap(retrievedBitmap);
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("Uh oh!")
                            .setMessage("Would you like to reset the image to Lucas?")
                            .setPositiveButton("Of course!", new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mMemeImage.setImageResource(R.drawable.lucas);
                                }
                            })
                            .setNegativeButton("Ah hellllllll na!", null)
                            .show();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_meme, menu);
        MenuItem shareItem = menu.findItem(R.id.menu_share);
        configureShareItem(shareItem);
        return super.onCreateOptionsMenu(menu);
    }

    private void configureShareItem(MenuItem item) {
        item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                View root = getWindow().getDecorView().findViewById(android.R.id.content);
                root.setDrawingCacheEnabled(true);
                root.buildDrawingCache();
                Bitmap bitmap = root.getDrawingCache();

                String path = "lucas-meme-" + SystemClock.elapsedRealtime() + ".jpg";
                File file = new File(Environment.getExternalStorageDirectory(), path);
                OutputStream outputStream = null;
                Throwable throwable = null;
                try {
                    outputStream = new FileOutputStream(file);
                    bitmap.compress(CompressFormat.JPEG, 100, outputStream);
                    root.setDrawingCacheEnabled(false);
                    root.destroyDrawingCache();
                } catch (FileNotFoundException e) {
                    throwable = e;
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            throwable = e;
                        }
                    }
                }
                if (throwable != null) {
                    new AlertDialog.Builder(LucasActivity.this)
                            .setTitle("There was an creating the image!")
                            // don't actually do this in production,
                            // it's a confusing error messages to users
                            .setMessage(throwable.getMessage())
                            .setPositiveButton("Dismiss", null)
                            .show();
                }

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                shareIntent.setType("image/jpeg");
                startActivity(shareIntent);

                return true; // consume the click event
            }
        });
    }
}