package francesco.workspace.homeapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
   
    public  class ConnectionDialogFragment extends DialogFragment {

        public static ConnectionDialogFragment newInstance() {
            ConnectionDialogFragment frag = new ConnectionDialogFragment();
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            return new AlertDialog.Builder(getActivity())
                    .setTitle("Connection Error")
                    .setPositiveButton("Retry",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            	((DialogClick) getActivity()).onClickPositive();
                            }
                        }
                    )
                    .setNegativeButton("Exit",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            	((DialogClick) getActivity()).onClickNegative();
                            }
                        }
                    )
                    .create();
        }
    }
