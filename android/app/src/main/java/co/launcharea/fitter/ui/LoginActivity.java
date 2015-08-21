package co.launcharea.fitter.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import co.launcharea.fitter.R;
import co.launcharea.fitter.util.FitterParseUtil;

public class LoginActivity extends BaseActionBarActivity {
    private EditText mUsernameEditText;
    private EditText mPasswordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mUsernameEditText = (EditText) findViewById(R.id.et_username);
        mPasswordEditText = (EditText) findViewById(R.id.et_password);

        mPasswordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onClickSignUp(mPasswordEditText);
                    return true;
                }
                return false;
            }
        });
    }

    public void onClickSignUp(View v) {
        ParseUser user = new ParseUser();
        user.setUsername(mUsernameEditText.getText().toString().trim());
        user.setPassword(mPasswordEditText.getText().toString().trim());
        user.put("name", user.getUsername());
        user.put("fitLogId", 0);

        showProgress(true);
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                showProgress(false);
                if (e != null) {
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    FitterParseUtil.addUserRelationWithInstallation();
                    finish();
                    startDispatchActivity();
                }
            }
        });
    }

    public void onClickSignIn(View v) {
        showProgress(true);
        ParseUser.logInInBackground(mUsernameEditText.getText().toString(), mPasswordEditText.getText()
                .toString(), new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                showProgress(false);
                if (e != null) {
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    FitterParseUtil.addUserRelationWithInstallation();
                    finish();
                    startDispatchActivity();
                }
            }
        });

    }

    private void startDispatchActivity() {
        Intent intent = new Intent(LoginActivity.this, DispatchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
