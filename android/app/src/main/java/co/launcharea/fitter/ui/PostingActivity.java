package co.launcharea.fitter.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import co.launcharea.fitter.model.FitLog;
import co.launcharea.fitter.model.Post;
import co.launcharea.fitter.widget.FitLogGroup;

public class PostingActivity extends BaseActionBarActivity implements FitLogGroup.OnDataChangedListener {
    public final static String EXTRA_WORKOUT_NAME = "extraWorkoutName";
    public final static String EXTRA_REFERENCE_ID = "extraReferenceId";
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int SELECT_WORKOUT_REQUEST = 2;
    public EditText editText;
    boolean ableToUpload = false;
    private MentionListFragment mMentionListFragment;
    private MentionInputController mMentionInputController;
    private View btnAddFitLog;
    private FitLogGroup fitLogGroup;
    private ParseObject mUserObject;
    private ParseFile mPhotoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(co.launcharea.fitter.R.layout.activity_posting);

        this.editText = (EditText) findViewById(co.launcharea.fitter.R.id.editPosting);
        this.btnAddFitLog = findViewById(co.launcharea.fitter.R.id.button_add_fitlog);
        this.fitLogGroup = (FitLogGroup) findViewById(co.launcharea.fitter.R.id.fitlog_group);

        btnAddFitLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PostingActivity.this.startActivityForResult(new Intent(PostingActivity.this, WorkoutSelectorActivity.class), SELECT_WORKOUT_REQUEST);
            }
        });
        findViewById(co.launcharea.fitter.R.id.root).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputMethodManager = (InputMethodManager) PostingActivity.this.getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                refreshSendButton();
            }
        });

        fitLogGroup.setOnDataChangedListener(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        mMentionListFragment = MentionListFragment.newInstance();
        fragmentManager.beginTransaction()
                .replace(co.launcharea.fitter.R.id.mention_list_container, mMentionListFragment)
                .commit();

        mMentionInputController = new MentionInputController(mMentionListFragment, editText);
    }

    private void refreshSendButton() {
        boolean able = fitLogGroup.hasData() || editText.getText().length() > 0;
        if (ableToUpload != able) {
            ableToUpload = able;
            invalidateOptionsMenu();
        }
    }

    private void showFitLogGroup(final String workoutName, final String referenceId) {
        if (btnAddFitLog.getVisibility() == View.VISIBLE) {
            showProgress(true);
            mUserObject = ParseObject.createWithoutData("_User", ParseUser.getCurrentUser().getObjectId());
            mUserObject.increment(FitLog.FIT_LOG_ID);
            mUserObject.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    // TODO
                    if (e == null) {
                        int newFitLogId = mUserObject.getInt(FitLog.FIT_LOG_ID);
                        fitLogGroup.initializeView(newFitLogId, workoutName, referenceId, true, btnAddFitLog);
                    } else {
                        Toast.makeText(PostingActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    showProgress(false);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(co.launcharea.fitter.R.menu.menu_posting, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem postItem = menu.findItem(co.launcharea.fitter.R.id.action_post);
        postItem.setEnabled(ableToUpload);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case co.launcharea.fitter.R.id.action_post:
                item.setEnabled(false);
                post();
                return true;
            case android.R.id.home:
                Toast.makeText(this, "Click home", Toast.LENGTH_SHORT).show();
                finish();
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void post() {
        showProgress(true);
        new AsyncTask<Void, Void, Exception>() {

            @Override
            protected Exception doInBackground(Void... params) {
                FitLog fitlog = fitLogGroup.getFitlog();
                if (fitlog != null) {
                    try {
                        ParseObject.saveAll(fitlog.getList());
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return e;
                    }
                }
                Post postObject = new Post();
                postObject.setContent(mMentionInputController.getContent());
                postObject.setUser(ParseUser.getCurrentUser());
                if (mPhotoFile != null) {
                    postObject.put("photo", mPhotoFile);
                }
                if (fitlog != null) {
                    postObject.setFitLogId(fitLogGroup.getFitlogId());
                    try {
                        postObject.setFitLogSummary(fitlog.toJsonArray().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return e;
                    }
                }
                try {
                    postObject.save();
                } catch (ParseException e) {
                    e.printStackTrace();
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception e) {
                super.onPostExecute(e);
                if (e == null) {
                    Toast.makeText(getApplicationContext(), "complete", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                invalidateOptionsMenu();
            }
        }.execute();
    }

    @Override
    public void onDataChanged() {
        refreshSendButton();
    }

    public void onClickPhotoButton(View v) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            String[] columns = {MediaStore.Images.Media.ORIENTATION};
            Cursor cursor = getContentResolver().query(uri, columns, null, null, null);
            if (cursor == null) {
                return;
            }
            cursor.moveToFirst();
            int degree = cursor.getInt(cursor.getColumnIndex(columns[0]));
            cursor.close();

            try {
                Bitmap src = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                Matrix matrix = new Matrix();
                matrix.postRotate(degree);
                Bitmap bitmap = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);;
                if (src.getWidth() > 1080) {
                    bitmap = Bitmap.createScaledBitmap(bitmap, 1080, (int) (bitmap.getHeight() * (1080.f / bitmap.getWidth())), false);
                }

                ImageView mPicture = (ImageView) findViewById(co.launcharea.fitter.R.id.photo);
                ImageLoader imageLoader = ImageLoader.getInstance();
                imageLoader.displayImage(uri.toString(), mPicture);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream);
                byte[] bytearray = stream.toByteArray();

                mPhotoFile = new ParseFile(String.format("media_%d.jpg", System.currentTimeMillis()), bytearray);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (requestCode == SELECT_WORKOUT_REQUEST && resultCode == RESULT_OK && data != null) {
            String workoutName = null;
            String referenceId = null;
            workoutName = data.getStringExtra(EXTRA_WORKOUT_NAME);
            if (data.hasExtra(EXTRA_REFERENCE_ID)) {
                referenceId = data.getStringExtra(EXTRA_REFERENCE_ID);
            }
            showFitLogGroup(workoutName, referenceId);
        }
    }
}
