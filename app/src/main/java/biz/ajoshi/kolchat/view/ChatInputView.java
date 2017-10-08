package biz.ajoshi.kolchat.view;

import biz.ajoshi.kolchat.R;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

/**
 * Shows an input area and a submit button. Button is enabled when text is entered into the edittext
 * // TODO do this in kotlin
 * Created by a.joshi on 10/7/17.
 */
public class ChatInputView extends RelativeLayout {

    /**
     * Listener called when the user taps on the submit/send button
     */
    public interface SubmitListener {
        /**
         * The user wants to submit this text so make a network call to do so
         *
         * @param text
         *         Text to submit to the server
         */
        void onSubmit(CharSequence text);
    }

    protected EditText inputField;
    protected ImageButton submitButton;
    protected SubmitListener listener;

    public ChatInputView(Context context) {
        super(context);
        initializeViews(context);
    }

    public ChatInputView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    /**
     * Inflates the views in the layout.
     *
     * @param context
     *         the current context for the view.
     */
    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            View view = inflater.inflate(R.layout.chat_input_view, this);
            inputField = view.findViewById(R.id.input);
            submitButton = view.findViewById(R.id.submit_area);
            inputField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                    shouldEnableSubmitButton(charSequence != null && charSequence.length() != 0);
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            shouldEnableSubmitButton(false);
            submitButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        // if we have a listener, tell it that we had a submit event
                        listener.onSubmit(inputField.getText());
                    }
                    // clear the edittext
                    inputField.setText("");
                }
            });
        }

    }

    /**
     * Enabled and disabled the submit button.
     *
     * @param shouldEnable
     *         true to enable the button, else false
     */
    private void shouldEnableSubmitButton(boolean shouldEnable) {
        submitButton.setEnabled(shouldEnable);
    }

    /**
     * Sets a new listener to be invoked when the submit button is tapped
     *
     * @param newListener
     */
    public void setSubmitListener(SubmitListener newListener) {
        listener = newListener;
    }

}