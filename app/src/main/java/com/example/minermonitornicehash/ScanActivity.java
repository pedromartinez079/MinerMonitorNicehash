package com.example.minermonitornicehash;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import com.google.zxing.Result;

import static com.example.minermonitornicehash.MainActivity.riginfoTV;
import static com.example.minermonitornicehash.MainActivity.riginfobutton;
import static com.example.minermonitornicehash.MainActivity.savewalletcb;
import static com.example.minermonitornicehash.MainActivity.walletaddress;

public class ScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView zXingScannerView;
    private int REQUESTCODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, REQUESTCODE);
        };
        zXingScannerView = new ZXingScannerView(getApplicationContext());
        setContentView(zXingScannerView);
        zXingScannerView.setResultHandler(this);
        zXingScannerView.startCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        zXingScannerView.setResultHandler(this);
        zXingScannerView.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        zXingScannerView.stopCamera();
        this.finish();
    }

    @Override
    public void handleResult(Result rawResult) {
        //Toast.makeText(getApplicationContext(),rawResult.getText(),Toast.LENGTH_SHORT).show();
        zXingScannerView.resumeCameraPreview(this);
        //MainActivity.riginfo.setText(rawResult.getText());
        walletaddress = rawResult.getText();
        if (savewalletcb.isChecked()){
            MainActivity.writeToFile(walletaddress, getApplicationContext());
        }
        riginfobutton.setEnabled(true);
        riginfoTV.setText("Please push Get Information button");
        onBackPressed();

    }
}
