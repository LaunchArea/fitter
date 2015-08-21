package co.launcharea.fitter.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import co.launcharea.fitter.R;
import co.launcharea.fitter.model.Comment;
import co.launcharea.fitter.model.Post;

public class PostDetailAcivity extends BaseActionBarActivity
{
    public static String POST_ID = "POST_ID";

    private Post mPost;
    private EditText mEditText;
    private View mCommentSendButton;

    private PostDetailFragment mPostDetailFragment;
    private MentionListFragment mMentionListFragment;

    private MentionInputController mMentionInputController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail_acivity);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mPost = ParseObject.createWithoutData(Post.class, extras.getString(POST_ID));
            Log.i("POST", "id:" + mPost.getObjectId());
        }

        initEditText();
        initCommentSendButton();

        FragmentManager fragmentManager = getSupportFragmentManager();
        mPostDetailFragment = PostDetailFragment.newInstance(mPost.getObjectId());
        fragmentManager.beginTransaction()
                .replace(R.id.post_detail_list_view, mPostDetailFragment)
                .commit();
        mMentionListFragment = MentionListFragment.newInstance();
        fragmentManager.beginTransaction()
                .replace(R.id.mention_list_container, mMentionListFragment)
                .commit();

        mMentionInputController = new MentionInputController(mMentionListFragment, mEditText);
    }

    private void initEditText() {
        mEditText = (EditText)findViewById(R.id.post_detail_edit_text);
        mEditText.setCursorVisible(false);

        mEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == mEditText.getId()) {
                    mEditText.setCursorVisible(true);
                }
            }
        });

        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        addComment(mMentionInputController.getContent());
                    }
                    return true;
                }

                return false;
            }
        });
    }

    private void initCommentSendButton() {
        mCommentSendButton = findViewById(R.id.comment_send_button);
        mCommentSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addComment(mMentionInputController.getContent());
            }
        });
    }

    private void addComment(String text) {

        if (text != null && text.length() > 0) {

            Comment comment = new Comment();
            comment.setContent(text);
            comment.setUser(ParseUser.getCurrentUser());
            comment.setPost(mPost);

            showProgress(true);
            comment.saveInBackground(new SaveCallback() {
                @Override
                public void done(com.parse.ParseException e) {
                    showProgress(false);
                    mEditText.setText("");
                    mPostDetailFragment.refresh();
                }
            });
        }
    }

    @Override
    public void onViewCreated(BaseFragment fragment) {
        super.onViewCreated(fragment);
    }
}
