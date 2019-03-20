package com.example.asus.ocrreaderwithtxttospeech;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.ocrreaderwithtxttospeech.PitchFragment.onInputPitchListner;
import com.example.asus.ocrreaderwithtxttospeech.SpeedFragment.onInputSpeedListner;
import com.example.asus.ocrreaderwithtxttospeech.utilities.AESUtils;
import com.example.asus.ocrreaderwithtxttospeech.utilities.OChist;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Locale;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity implements onInputPitchListner,
        onInputSpeedListner {

    private TextView statusMessage;
    private TextView textValue;
    private Button copyButton;
    private Button mailTextButton;
    private Button textTospeechButton;
    private Button save;
    private Button history;
    boolean isPlaying = false;
    private float pitch,speed;

    private static final int RC_OCR_CAPTURE = 9003;
    private static final String TAG = "MainActivity";

    private TextToSpeech textToSpeech;
    AsyncHttpClient client;

    RequestParams params;
    JSONObject obj1;

    String url="http://srishti-systems.info/projects/ocr/string.php?";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusMessage = (TextView)findViewById(R.id.status_message);
        textValue = (TextView)findViewById(R.id.text_value);


        Button readTextButton = (Button) findViewById(R.id.read_text_button);
        readTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // launch Ocr capture activity.
                Intent intent = new Intent(getApplicationContext(), OcrCaptureActivity.class);
                intent.putExtra(OcrCaptureActivity.AutoFocus, true);

                startActivityForResult(intent, RC_OCR_CAPTURE);
            }
        });

        copyButton = (Button) findViewById(R.id.copy_text_button);
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    android.text.ClipboardManager clipboard =
                            (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(textValue.getText().toString());
                } else {
                    android.content.ClipboardManager clipboard =
                            (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", textValue.getText().toString());
                    clipboard.setPrimaryClip(clip);
                }
                Toast.makeText(getApplicationContext(), R.string.clipboard_copy_successful_message, Toast.LENGTH_SHORT).show();
            }
        });

        mailTextButton = (Button) findViewById(R.id.mail_text_button);
        mailTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
//                sharingIntent.setType("message/rfc822");
//                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, textValue.getText().toString());
//                startActivity(Intent.createChooser(sharingIntent,"Share using"));
                String whatsAppMessage = textValue.getText().toString();

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, whatsAppMessage);
                sendIntent.setType("text/plain");

                // Do not forget to add this to open whatsApp App specifically
                sendIntent.setPackage("com.whatsapp");
                startActivity(sendIntent);
                // Intent i = new Intent(Intent.ACTION_SEND);
//                i.setType("message/rfc822");
//                i.putExtra(Intent.EXTRA_SUBJECT, "Text Read");
//                i.putExtra(Intent.EXTRA_TEXT, textValue.getText().toString());
//                try {
//                    startActivity(Intent.createChooser(i, getString(R.string.mail_intent_chooser_text)));
//                } catch (android.content.ActivityNotFoundException ex) {
//                    Toast.makeText(getApplicationContext(),
//                            R.string.no_email_client_error, Toast.LENGTH_SHORT).show()
  //              }
            }
        });

        textToSpeech = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
              if (status == TextToSpeech.SUCCESS){
                  int result = textToSpeech.setLanguage(Locale.ENGLISH);
                  if (result == TextToSpeech.LANG_MISSING_DATA
                          || result == TextToSpeech.LANG_NOT_SUPPORTED){
                      Log.e("Text to speech","Language not supported");
                  }else {
                      textTospeechButton.setEnabled(true);
                  }
              }else {
                  Log.e("Text to speech","Initialization failed");
              }
            }
        });

        textTospeechButton = (Button)findViewById(R.id.text_to_speech_button);
        textTospeechButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                   textToSpeech.stop();
                   textTospeechButton.setText("Speak");
                }else{
                    textTospeechButton.setText("Stop");
                    speak();
                }
                isPlaying = !isPlaying;




            }
        });

        registerForContextMenu(textTospeechButton);
        client = new AsyncHttpClient();
        params = new RequestParams();

        save=findViewById(R.id.save_text_button);
        SharedPreferences sp=getApplicationContext()
                .getSharedPreferences("d1",MODE_PRIVATE);
        final String sid=sp.getString("did",null);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(textValue.getText()=="")
                {
                    Toast.makeText(getApplicationContext(),"Empty text field",Toast.LENGTH_SHORT).show();
                }
                else {
                    LayoutInflater inflat = LayoutInflater.from(MainActivity.this);
                    View cuslay = inflat.inflate(R.layout.title, null);

                    final EditText title = cuslay.findViewById(R.id.etitle);
                    Button Bsave = cuslay.findViewById(R.id.bsave);
                    AlertDialog.Builder AB = new AlertDialog.Builder(MainActivity.this);
                    AB.setView(cuslay);
                    final AlertDialog A = AB.create();
                    A.show();
                    Bsave.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            A.cancel();
                            String stitle = title.getText().toString();
                            String encrypted = "";
                            String sourceStr = textValue.getText().toString();
                            try {
                                encrypted = AESUtils.encrypt(sourceStr);
                                Log.d("TEST", "encrypted:" + encrypted);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            params.put("string", encrypted);
                            params.put("title", stitle);
                            params.put("id", sid);
                            client.get(url, params, new AsyncHttpResponseHandler() {

                                @Override
                                public void onSuccess(String content) {
                                    super.onSuccess(content);

                                    try {
                                        Log.e("innn", "in");

                                        obj1 = new JSONObject(content);

                                        String s = obj1.getString("status");

                                        Toast.makeText(MainActivity.this, "" + s,
                                                Toast.LENGTH_SHORT).show();
                                        if (s.equals("Success")) {

                                            Toast.makeText(MainActivity.this, "Saved successfully", Toast.LENGTH_SHORT).show();


                                        }


                                    } catch (Exception e) {

                                    }
                                }
                            });


                        }
                    });

                }
                }
        });
        history=findViewById(R.id.history_text_button);
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent k=new Intent(MainActivity.this, OChist.class);
                startActivity(k);
            }
        });
        String s= getIntent().getStringExtra("key1");
        textValue.setText(s);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        menu.setHeaderTitle("Select your Action");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.pitchOption){
            PitchFragment pitchFragment = new PitchFragment();
            pitchFragment.show(getSupportFragmentManager(),"PitchFargment");
        }
        if (item.getItemId() == R.id.speedOption){
           SpeedFragment speedFragment = new SpeedFragment();
           speedFragment.show(getFragmentManager(),"SpeedFragment");
        }
        else {
            return false;}
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (textValue.getText().toString().isEmpty()) {
            copyButton.setVisibility(View.GONE);
            mailTextButton.setVisibility(View.GONE);
            textTospeechButton.setVisibility(View.GONE);
        } else {
            copyButton.setVisibility(View.VISIBLE);
            mailTextButton.setVisibility(View.VISIBLE);
            textTospeechButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == RC_OCR_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    String text = data.getStringExtra(OcrCaptureActivity.TextBlockObject);
                    statusMessage.setText(R.string.ocr_success);
                    textValue.setText(text);
                    Log.d(TAG, "Text read: " + text);
                } else {
                    statusMessage.setText(R.string.ocr_failure);
                    Log.d(TAG, "No Text captured, intent data is null");
                }
            } else {
                statusMessage.setText(String.format(getString(R.string.ocr_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void speak(){

        String text = textValue.getText().toString();
        textToSpeech.setPitch(pitch);
        textToSpeech.setSpeechRate(speed);
        textToSpeech.speak(text,TextToSpeech.QUEUE_FLUSH,null);
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void sentPitchInput(String input) {
        pitch = (float)Float.parseFloat(input);
    }

    @Override
    public void sentSpeedInput(String input) {
        speed = (float)Float.parseFloat(input);
    }

}

