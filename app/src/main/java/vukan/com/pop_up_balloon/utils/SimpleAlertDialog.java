package vukan.com.pop_up_balloon.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class SimpleAlertDialog extends DialogFragment {

    private static final String TITLE_KEY = "title_key";
    private static final String MESSAGE_KEY = "message_key";

    public SimpleAlertDialog() {
    }

    @NonNull
    public static SimpleAlertDialog newInstance(String title, String message) {
        Bundle args = new Bundle();
        args.putString(TITLE_KEY, title);
        args.putString(MESSAGE_KEY, message);
        SimpleAlertDialog fragment = new SimpleAlertDialog();
        fragment.setArguments(args);
        return fragment;
    }

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