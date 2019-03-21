package com.example.asus.ocrreaderwithtxttospeech.utilities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.ocrreaderwithtxttospeech.MainActivity;
import com.example.asus.ocrreaderwithtxttospeech.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

public class Login extends AppCompatActivity {
    AsyncHttpClient client;
    RequestParams params;
    JSONObject obj1;
    TextInputLayout userphone,passp;
    String url="http://srishti-systems.info/projects/ocr/login.php?";

    SharedPreferences shared;
    Button login,signup;
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        shared = getSharedPreferences("login",MODE_PRIVATE);

        if(shared.getBoolean("logged",false)) {
            Intent in = new Intent(Login.this, MainActivity.class);
            startActivity(in);
        }
        client=new AsyncHttpClient();
        params=new RequestParams();
        userphone=findViewById(R.id.ocrphone);
        passp=findViewById(R.id.ocrpass);
        login=findViewById(R.id.login);
        signup=findViewById(R.id.signup);
        textView=findViewById(R.id.forget);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ik=new Intent(Login.this,Forget_password.class);
                startActivity(ik);

            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                params.put("email", userphone.getEditText().getText().toString());
                params.put("password", passp.getEditText().getText().toString());

                client.get(url, params, new AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(String content) {
                        super.onSuccess(content);

                        try {
                            Log.e("innn", "in");

                            obj1 = new JSONObject(content);

                            String s = obj1.getString("status");

                            Toast.makeText(Login.this, "" + s, Toast.LENGTH_SHORT).show();
                            if (s.equals("Success")) {
                                JSONObject obj2=obj1.getJSONObject("User_data");
                                SharedPreferences sp=getApplicationContext()
                                        .getSharedPreferences("d1",MODE_PRIVATE);
                                SharedPreferences.Editor ed=sp.edit();
                                ed.putString("did",obj2.getString("id"));
                                ed.putString("dname",obj2.getString("name"));
                                ed.putString("demail",obj2.getString("email"));
                                ed.putString("dpass",obj2.getString("password"));

                                ed.commit();

                                Intent i = new Intent(Login.this, MainActivity.class);
                                startActivity(i);

                                shared.edit().putBoolean("logged",true).apply();
                            }


                        } catch (Exception e) {

                        }
                    }
                });

            }
        });


                signup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent j = new Intent(Login.this, Registration.class);
                        startActivity(j);
                    }
                });
            }

        }