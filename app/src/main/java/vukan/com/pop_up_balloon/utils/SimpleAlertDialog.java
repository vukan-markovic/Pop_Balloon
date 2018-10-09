package vukan.com.pop_up_balloon.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

/**
 * <h1>SimpleAlertDialog</h1>
 *
 * <p><b>SimpleAlertDialog</b> class is responsible to show user dialog when he reach new high score.</p>
 */
public class SimpleAlertDialog extends DialogFragment {
    private static final String TITLE_KEY = "title_key", MESSAGE_KEY = "message_key";

    public SimpleAlertDialog() {
    }

    /**
     * This method create, define and return new instance of SimpleAlertDialog class.
     *
     * @param title   Title of the dialog.
     * @param message Message of the dialog contain new high score.
     * @return SimpleAlertDialog New instance of class.
     * @see Bundle
     */
    @NonNull
    public static SimpleAlertDialog newInstance(String title, String message) {
        Bundle args = new Bundle();
        args.putString(TITLE_KEY, title);
        args.putString(MESSAGE_KEY, message);
        SimpleAlertDialog fragment = new SimpleAlertDialog();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * This method create, define, build and return new instance of AlertDialog class.
     *
     * @param savedInstanceState Define potentially saved parameters due to configurations changes.
     * @return Dialog New instance of AlertDialog.Builder class
     * @see Dialog
     * @see Bundle
     * @see AlertDialog.Builder
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args == null) throw new AssertionError();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(args.getString(TITLE_KEY))
                .setMessage(args.getString(MESSAGE_KEY))
                .setCancelable(false);

        builder.setPositiveButton(android.R.string.ok, null);
        return builder.create();
    }
}