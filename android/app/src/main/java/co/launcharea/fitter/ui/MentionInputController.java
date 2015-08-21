package co.launcharea.fitter.ui;

import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.widget.EditText;

import com.parse.ParseUser;

/**
 * Created by hyungchulkim on 8/14/15.
 */
public class MentionInputController implements MentionListFragment.MentionListener {
    private EditText mEditText;
    private MentionListFragment mMentionListFragment;

    public MentionInputController(MentionListFragment fragment, EditText editText) {
        mMentionListFragment = fragment;
        mMentionListFragment.setMentionListener(this);

        mEditText = editText;
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (before == 1 && count == 0) {
                    // 공백이 지워지는 경우는 무시
                    return;
                }
                String[] array = s.toString().split(" ");
                int cursor = mEditText.getSelectionEnd();

                int totalCharacters = 0;
                for (int i = 0; i < array.length; i++) {
                    String word = array[i];

                    // 커서가 @가 포함된 단어에 위치할 때
                    if (word.length() > 0 && word.charAt(0) == '@' && (totalCharacters <= cursor && cursor <= totalCharacters + word.length())) {
                        mMentionListFragment.visibleMentionList(word.substring(1));
                        return;
                    }
                    totalCharacters += word.length() + 1;
                }

                mMentionListFragment.visibleMentionList(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
                handleCorruptedSpan(s);
            }

            private void handleCorruptedSpan(Editable s) {
                MentionSpan[] spans = s.getSpans(0, s.length(), MentionSpan.class);
                for (MentionSpan each : spans) {
                    int start = s.getSpanStart(each);
                    int end = s.getSpanEnd(each);
                    CharSequence text = s.subSequence(start, end);
                    if (!each.isValid(text)) {
                        s.removeSpan(each);
                        s.delete(start, end);
                    }
                }
            }
        });
    }

    @Override
    public void onClickUser(ParseUser user) {
        int cursorPosition = mEditText.getSelectionEnd();
        if (cursorPosition == 0) {
            return;
        }
        Editable editable = mEditText.getEditableText();
        String[] array = editable.toString().split(" ");

        int totalCharacters = 0;
        for (int i = 0; i < array.length; i++) {
            String word = array[i];

            // 커서가 @가 포함된 단어에 위치할 때
            if (word.length() > 0 && word.charAt(0) == '@' && (totalCharacters <= cursorPosition && cursorPosition <= totalCharacters + word.length())) {
                // 커서 위치의 단어 제거
                editable.delete(totalCharacters, totalCharacters + word.length());
                cursorPosition -= word.length();
                break;
            }
            totalCharacters += word.length() + 1;
        }

        // @username 으로 bold span 등록
        String atUsername = "@" + user.getUsername();
        int spanStart = cursorPosition;
        int spanEnd = cursorPosition + atUsername.length();
        MentionSpan span = new MentionSpan(atUsername);
        editable.insert(cursorPosition, atUsername + " ");
        editable.setSpan(span, spanStart, spanEnd, 0);

        mEditText.setText(editable);
        mEditText.setSelection(cursorPosition + atUsername.length() + 1);
        mMentionListFragment.visibleMentionList(null);
    }

    public String getContent() {
        Editable s = mEditText.getEditableText();
        int cutOffset = 0;
        StringBuilder builder = new StringBuilder();
        MentionSpan[] spans = s.getSpans(0, s.length(), MentionSpan.class);
        for (MentionSpan each : spans) {
            int start = s.getSpanStart(each);
            int end = s.getSpanEnd(each);
            builder.append(s.subSequence(cutOffset, start));
            CharSequence text = s.subSequence(start, end);
            if (each.isValid(text)) {
                builder.append(each.getContent());
            } else {
                builder.append(text);
            }
            cutOffset = end;
        }
        builder.append(s.subSequence(cutOffset, s.length()));
        return builder.toString();
    }

    class MentionSpan extends StyleSpan {
        private final String mOriginal;

        public MentionSpan(String text) {
            super(Typeface.BOLD);
            mOriginal = text;
        }

        public String getContent() {
            return "\"<" + mOriginal + ">\"";
        }

        public boolean isValid(CharSequence text) {
            return mOriginal.equalsIgnoreCase(String.valueOf(text));
        }
    }
}
