package org.feup.cm.acmeapp.Fragments;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.feup.cm.acmeapp.R;
import org.feup.cm.acmeapp.SharedViewModel;
import org.feup.cm.acmeapp.model.Purchase;

import java.nio.charset.Charset;

public class NFCFragment extends Fragment implements NfcAdapter.OnNdefPushCompleteCallback{
    private SharedViewModel sharedViewModel;
    private NfcAdapter nfcAdapter;
    private Purchase purchase;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_n_f_c, container, false);
        sharedViewModel = ViewModelProviders.of(requireActivity()).get(SharedViewModel.class);

        purchase = sharedViewModel.getPurchase();

        // Check for available NFC Adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(getContext());
        if (nfcAdapter == null) {
            Toast.makeText(getContext(), "NFC is not available on this device.", Toast.LENGTH_LONG).show();
        }

        NdefMessage msg = new NdefMessage(new NdefRecord[] {NdefRecord.createApplicationRecord(purchase.toString())});

        nfcAdapter.setNdefPushMessage(msg, getActivity());
        nfcAdapter.setOnNdefPushCompleteCallback(this, getActivity());
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getActivity().getApplicationContext(), "Message sent.", Toast.LENGTH_LONG).show();
            }
        });
    }
}