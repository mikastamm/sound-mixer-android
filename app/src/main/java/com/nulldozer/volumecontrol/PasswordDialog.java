package com.nulldozer.volumecontrol;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Mika on 04.11.2017.
 */

public class PasswordDialog extends DialogFragment {

    private final static String TAG = "PasswordDialog";
    private VolumeServer server;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.password_dialog, container, false);
    }

    public void setServer(VolumeServer server)
    {
        this.server = server;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        Button btnOkay, btnCancel;
        btnCancel = (Button) view.findViewById(R.id.btnPasswordCancel);
        btnOkay = (Button) view.findViewById(R.id.btnPasswordOkay);
        final EditText editTextPassword = (EditText)view.findViewById(R.id.editTextPassword) ;

        btnOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor prefEditor = MainActivity.Instance.getPreferences(Context.MODE_PRIVATE).edit();
                server.standardPassword = editTextPassword.getText().toString();
                prefEditor.putString(PrefKeys.ServerStandardPasswordPrefix + VCCryptography.getMD5Hash(server.RSAPublicKey), server.standardPassword);
                prefEditor.apply();
                MainActivity.Instance.serverListViewAdapter.setActive(server);
                dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
