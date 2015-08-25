package co.launcharea.fitter.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import co.launcharea.fitter.R;

public class ProfileEditActivity extends BaseActionBarActivity implements View.OnClickListener {
    private static final int PICK_IMAGE_REQUEST = 1;

    private TextView mName;
    private ParseImageView mPicture;
    private TextView mBodyStatus;
    private Number mHeight = null;
    private Number mWeight = null;
    private Number mFat = null;
    private View mHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);
        mHeader = getLayoutInflater().inflate(R.layout.header_profile_edit, null);

        mName = (TextView) mHeader.findViewById(R.id.name);
        mPicture = (ParseImageView) mHeader.findViewById(R.id.profile_picture);
        mBodyStatus = (TextView) mHeader.findViewById(R.id.body_status);

        mName.setOnClickListener(this);
        mPicture.setOnClickListener(this);
        mBodyStatus.setOnClickListener(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        PostListFragment fragment = PostListFragment.newInstance(PostListFragment.PostScope.USER, ParseUser.getCurrentUser().getObjectId());
        fragmentManager.beginTransaction()
                .replace(R.id.post_list_container, fragment)
                .commit();
    }

    private void refreshProfile() {
        ParseUser me = ParseUser.getCurrentUser();
        String name = me.getString("name");

        mName.setText(name);
        ParseFile file = me.getParseFile("picture");
        if (file != null) {
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(file.getUrl(), mPicture);
        }

        getSupportActionBar().setTitle(me.getUsername() + getString(R.string.profile_title_postfix));
    }

    private void refreshBodyStatus() {
        showProgress(true);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Profile");
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.orderByDescending("createdAt");
        query.setLimit(1);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                showProgress(false);
                if (e != null) {
                    Toast.makeText(ProfileEditActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    if (list.size() > 0) {
                        ParseObject obj = list.get(0);
                        mHeight = obj.getNumber("height");
                        mWeight = obj.getNumber("weight");
                        mFat = obj.getNumber("bodyFat");
                        mBodyStatus.setText(getString(R.string.label_height) + ": " + (mHeight == null ? "-" : mHeight) +
                                        "\n" + getString(R.string.label_weight) + ": " + (mWeight == null ? "-" : mWeight) +
                                        "\n" + getString(R.string.label_body_fat) + ": " + (mFat == null ? "-" : mFat)
                        );
                    }
                }
            }
        });
    }

    private EditText makeEditText(String hint, CharSequence prefill) {
        EditText et = new EditText(this);
        et.setHint(hint);
        et.setText(prefill);
        et.setSelection(et.getText().length());

        return et;
    }

    public void onClickName(View v) {
        final EditText name = makeEditText("Name", mName.getText());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(name);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showProgress(true);
                ParseUser me = ParseUser.getCurrentUser();
                me.put("name", name.getText().toString());
                me.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        showProgress(false);
                        refreshProfile();
                    }
                });
            }
        });
        builder.show();
    }


    public void onClickBodyStatus(View v) {
        final EditText height = makeEditText("Height", mHeight == null ? "" : mHeight.toString());
        final EditText weight = makeEditText("Weight", mWeight == null ? "" : mWeight.toString());
        final EditText fat = makeEditText("Body Fat", mFat == null ? "" : mFat.toString());

        height.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        weight.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        fat.setRawInputType(InputType.TYPE_CLASS_NUMBER);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(height);
        layout.addView(weight);
        layout.addView(fat);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(layout);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showProgress(true);

                ParseObject object = new ParseObject("Profile");
                object.put("user", ParseUser.getCurrentUser());
                object.put("height", Integer.parseInt(height.getText().toString()));
                object.put("weight", Integer.parseInt(weight.getText().toString()));
                object.put("bodyFat", Integer.parseInt(fat.getText().toString()));
                object.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        showProgress(false);
                        refreshBodyStatus();
                    }
                });
            }
        });
        builder.show();
    }

    public void onClickProfilePicture(View v) {
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
                Bitmap bitmap = ThumbnailUtils.extractThumbnail(src, 160, 160, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                Matrix matrix = new Matrix();
                matrix.postRotate(degree);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                mPicture.setImageBitmap(bitmap);

                storePictureInBackground(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void storePictureInBackground(Bitmap bitmap) {
        showProgress(true);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream);
        byte[] bytearray = stream.toByteArray();

        final ParseFile file = new ParseFile(String.format("picture_%d.jpg", System.currentTimeMillis()), bytearray);
        file.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    showProgress(false);
                    Toast.makeText(ProfileEditActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    ParseUser me = ParseUser.getCurrentUser();
                    me.put("picture", file);
                    me.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Toast.makeText(ProfileEditActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            showProgress(false);
                        }
                    });
                }
            }
        });


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.name:
                onClickName(v);
                break;
            case R.id.body_status:
                onClickBodyStatus(v);
                break;
            case R.id.profile_picture:
                onClickProfilePicture(v);
                break;
        }
    }

    @Override
    public void onViewCreated(BaseFragment fragment) {
        super.onViewCreated(fragment);
        if (fragment instanceof PostListFragment) {
            ListView listView = ((PostListFragment) fragment).getListView();
            ListAdapter adapter = listView.getAdapter();
            listView.setAdapter(null);
            listView.addHeaderView(mHeader);
            listView.setAdapter(adapter);

            refreshProfile();
            refreshBodyStatus();
        }
    }
}
