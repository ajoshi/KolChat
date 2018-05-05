package biz.ajoshi.kolchat.chat.view;

import biz.ajoshi.kolchat.chat.R;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

/**
 * Shows an input area and a submit button. Button is enabled when text is entered into the edittext
 * // TODO do this in kotlin
 * // TODO add buttons for common chat commands like /whois, /who, /count, >:(
 * Created by ajoshi on 10/7/17.
 */
public class ChatInputView extends RelativeLayout {

    /**
     * Listener called when the user taps on the submit/send button
     */
    protected interface SubmitListener {
        /**
         * The user wants to submit this text so make a network call to do so
         *
         * @param text
         *         Text to submit to the server
         */
        void onSubmit(CharSequence text);
    }

    private EditText inputField;
    private ImageButton submitButton;
    private SubmitListener listener;

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
            // submit button is disabled when view is created (there is no text to submit)
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
            // allow users to press then keyboard 'send' button to send
            inputField.setOnEditorActionListener(new OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    boolean eventHandled = false;
                    if (actionId == EditorInfo.IME_ACTION_SEND) {
                        submitButton.callOnClick();
                        eventHandled = true;
                    }
                    return eventHandled;
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
     *         the listener to be called when submit button is tapped. Replaces the old listener
     */
    public void setSubmitListener(SubmitListener newListener) {
        listener = newListener;
    }

    /**
     * Sets the input field text to the given value
     *
     * @param text
     *         new value of the input field
     */
    public void setInputText(CharSequence text) {
        inputField.setText(text);
    }

    /**
     * Returns the input field text
     *
     * @return current value of the input field
     */
    public Editable getInputText() {
        return inputField.getText();
    }

    /**
     * Appends the given text to whatever is currently in the input field
     *
     * @param text
     *         CharSequence to append to the input field
     *
     * @return the new input field value
     */
    public Editable appendInputText(CharSequence text) {
        return inputField.getText().append(text);
    }

}
