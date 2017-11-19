package com.example.daniel.m117;

import android.bluetooth.BluetoothAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.content.Intent;

import com.wahoofitness.connector.HardwareConnector;
import com.wahoofitness.connector.HardwareConnectorEnums;
import com.wahoofitness.connector.HardwareConnectorTypes;
import com.wahoofitness.connector.capabilities.Capability;
import com.wahoofitness.connector.capabilities.Heartrate;
import com.wahoofitness.connector.conn.connections.SensorConnection;
import com.wahoofitness.connector.conn.connections.params.ConnectionParams;
import com.wahoofitness.connector.listeners.discovery.DiscoveryListener;

import java.util.Locale;

import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements OnInitListener{

    private TextView voiceInput;
    private TextView speakButton;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private int MY_DATA_CHECK_CODE = 0;
    private TextToSpeech myTTS;

    //WAHOO STUFF
    private HardwareConnector mHardwareConnector;
    private final HardwareConnector.Callback mHardwareConnectorCallback= new HardwareConnector.Callback() {
        @Override
        public void connectorStateChanged(HardwareConnectorTypes.NetworkType networkType, HardwareConnectorEnums.HardwareConnectorState hardwareConnectorState) {

        }

        @Override
        public void connectedSensor(SensorConnection sensorConnection) {

        }

        @Override
        public void disconnectedSensor(SensorConnection sensorConnection) {

        }

        @Override
        public void hasData() {

        }

        @Override
        public void onFirmwareUpdateRequired(SensorConnection sensorConnection, String s, String s1) {

        }
    };
    private SensorConnection mSensorConnection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //bluetooth

        //SERVICE!
        //Log.d("myTag","start");
        //startService(new Intent(getBaseContext(), MyService.class));
        //Log.d("myTag","done with start");

        DiscoveryListener me = new DiscoveryListener() {
            @Override
            public void onDeviceDiscovered(ConnectionParams connectionParams) {
                Log.d("myTag","found matching device!");
                mSensorConnection = mHardwareConnector.requestSensorConnection(connectionParams,mSensorConnectionListener);
                if (mSensorConnection!=null){
                    Log.d("sensors","sensor connection succeeded ");
                    mHardwareConnector.stopDiscovery(HardwareConnectorTypes.NetworkType.BTLE);
                }else
                    Log.d("sensors","failure");
            }

            @Override
            public void onDiscoveredDeviceLost(ConnectionParams connectionParams) {

            }

            @Override
            public void onDiscoveredDeviceRssiChanged(ConnectionParams connectionParams, int i) {

            }
        };
        mHardwareConnector = new HardwareConnector(this, mHardwareConnectorCallback);
        mHardwareConnector.startDiscovery(HardwareConnectorTypes.SensorType.HEARTRATE, HardwareConnectorTypes.NetworkType.BTLE,
                me);

        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

        voiceInput = (TextView) findViewById(R.id.voiceInput);
        speakButton = (TextView) findViewById(R.id.btnSpeak);

        speakButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                askSpeechInput();
            }
        });

    }
    public void onInit(int initStatus) {
        if (initStatus == TextToSpeech.SUCCESS) {
            myTTS.setLanguage(Locale.US);
        }
    }
    // Showing google speech input dialog

    private void askSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Hi speak something");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {

        }
    }
    private void speakWords(String speech) {
    //implement TTS here
        myTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    }

    // Receiving speech input

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                myTTS = new TextToSpeech(this, this);
            }
            else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    Log.d("myTag", "This is my message");
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (result.get(0).equals("tired")){
                        voiceInput.setText("command recognized");
                        int rate = getHeartrateData().getHeartrateBpm();
                        String st = getHeartrateData().toString();
                        speakWords(""+rate);
                    } else {
                        int rate = getHeartrateData().getHeartrateBpm();
                        String st = getHeartrateData().toString();
                        speakWords(""+rate);
                    }
                }
                break;
            }
        }
    }
    Heartrate.Data getHeartrateData()
    {
        if(mSensorConnection!=null){
            Heartrate heartrate=(Heartrate)mSensorConnection.getCurrentCapability(Capability.CapabilityType.Heartrate);
            if(heartrate!=null){
                return heartrate.getHeartrateData();
            }else{
//Thesensorconnectiondoesnotcurrentlysupporttheheartratecapability
                return null;
            }
        }else{
//Sensornotconnected
            return null;
        }
    }
    private final SensorConnection.Listener mSensorConnectionListener = new SensorConnection.Listener() {
        @Override
        public void onSensorConnectionStateChanged(SensorConnection sensorConnection, HardwareConnectorEnums.SensorConnectionState sensorConnectionState) {
        }

        @Override
        public void onSensorConnectionError(SensorConnection sensorConnection, HardwareConnectorEnums.SensorConnectionError sensorConnectionError) {

        }

        @Override
        public void onNewCapabilityDetected(SensorConnection sensorConnection, Capability.CapabilityType capabilityType) {
            if(capabilityType== Capability.CapabilityType.Heartrate){
                Heartrate heartrate=(Heartrate)sensorConnection.getCurrentCapability(Capability.CapabilityType.Heartrate);
                //heartrate.addListener(new Heartrate.Listener mHeartRateListener);
            }
        }
    };
}
