//start a workout and display: "enjoy your workout" message

package com.ledangaravi.slink;


import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.amazonaws.models.nosql.DEVICESDO;
import com.amazonaws.models.nosql.WODDO;
import com.google.android.gms.vision.barcode.Barcode;
import com.notbytes.barcode_reader.BarcodeReaderActivity;

import static com.ledangaravi.slink.AuthenticatorActivity.dynamoDBMapper;
import static com.ledangaravi.slink.IntroActivity.username;

public class EnjoyActivity extends AppCompatActivity {
    private static final int BARCODE_READER_ACTIVITY_REQUEST = 1208;

    public static String deviceID = "defaultDevice";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_enjoy);

        ImageView imageView = (ImageView) findViewById(R.id.enjoy_imageView);
        TextView textView = (TextView) findViewById(R.id.enjoy_textView);
        Button button = (Button) findViewById(R.id.enjoy_button);

        imageView.setVisibility(View.INVISIBLE);
        textView.setVisibility(View.INVISIBLE);
        button.setVisibility(View.INVISIBLE);

        Intent launchIntent = BarcodeReaderActivity.getLaunchIntent(this, true, false);
        startActivityForResult(launchIntent, BARCODE_READER_ACTIVITY_REQUEST);

    }

    public void submitWOD(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                //todo get correct WOD

                //get a demo WOD
                WODDO wodItem = dynamoDBMapper.load(WODDO.class,"slink", "Demo");

                final DEVICESDO devicesItem = new DEVICESDO();

                devicesItem.setDeviceID(deviceID);
                devicesItem.setUName(username);
                devicesItem.setWODName(wodItem.getWODName());
                devicesItem.setExList(wodItem.getExList());
                devicesItem.setRepList(wodItem.getRepList());
                devicesItem.setWList(wodItem.getWList());

                dynamoDBMapper.save(devicesItem);

            }
        }).start();

    }

    public void backHome(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ImageView imageView = (ImageView) findViewById(R.id.enjoy_imageView);
        TextView textView = (TextView) findViewById(R.id.enjoy_textView);
        Button button = (Button) findViewById(R.id.enjoy_button);

        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(this, R.string.qr_scan_fail, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return;
        }

        if (requestCode == BARCODE_READER_ACTIVITY_REQUEST && data != null) {
            Barcode barcode = data.getParcelableExtra(BarcodeReaderActivity.KEY_CAPTURED_BARCODE);
            deviceID = barcode.rawValue;

            imageView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.VISIBLE);
            button.setVisibility(View.VISIBLE);

            //check if a valid device ID, needs to contain "slink"
            if(barcode.rawValue.toLowerCase().contains("slink")){
                deviceID = barcode.rawValue;
                submitWOD();
            }else{
                Toast.makeText(this, R.string.invalid_qr, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
        }
    }
}
