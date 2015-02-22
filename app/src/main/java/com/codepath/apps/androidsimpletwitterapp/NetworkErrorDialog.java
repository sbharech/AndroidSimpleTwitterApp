package com.codepath.apps.androidsimpletwitterapp;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by suraj on 05/02/15.
 */

public class NetworkErrorDialog {
    private Context context;

    public NetworkErrorDialog(Context context) {
        this.context = context;
    }

    public void createNetworkErrorDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(R.string.network_error);

        alertDialogBuilder.setPositiveButton(R.string.ok_button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
