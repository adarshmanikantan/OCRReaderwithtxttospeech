package com.example.asus.ocrreaderwithtxttospeech.utilities;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.asus.ocrreaderwithtxttospeech.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

public class Registration extends AppCompatActivity {

    TextInputLayout name,emaill,password,confirm;
    Button reg;
    AsyncHttpClient client;
    RequestParams params;
    JSONObject object;
    String url="http://srishti-systems.info/projects/ocr/reg.php?";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        name = findViewById(R.id.input_name);
        emaill = findViewById(R.id.input_phone);
        password = findViewById(R.id.input_password);
        reg = findViewById(R.id.btn_signup);
        confirm = findViewById(R.id.input_confirmpassword);
        client = new AsyncHttpClient();
        params = new RequestParams();


        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (name.getEditText().getText().toString().equals("")) {
                    name.setError("Input details");
                } else if (emaill.getEditText().getText().toString().equals("")) {
                    emaill.setError("Input details");
                } else if (password.getEditText().getText().toString().equals("")) {
                    password.setError("Input details");
                } else if (confirm.getEditText().getText().toString().equals("")) {
                    confirm.setError("Input details");
                } else if (!confirm.getEditText().getText().toString().equals(password.getEditText().getText().toString())) {
                    confirm.setError("Passwords not matching");
                } else {

                    params.put("name", name.getEditText().getText().toString());
                    params.put("email", emaill.getEditText().getText().toString());
                    params.put("password", password.getEditText().getText().toString());


                    client.get(url, params, new AsyncHttpResponseHandler() {

                        @Override
                        public void onSuccess(String content) {
                            super.onSuccess(content);

                            try {
                                object = new JSONObject(content);

                                if (object.getString("status").equals("success")) {
                                    Toast.makeText(Registration.this, "" + object.getString("status"), Toast.LENGTH_SHORT).show();

                                    Intent l = new Intent(Registration.this, Login.class);
                                    startActivity(l);
                                } else {
                                    Toast.makeText(Registration.this, "" + object.getString("status"), Toast.LENGTH_SHORT).show();
                                }

                            } catch (Exception e) {

                            }


                        }
                    });
                }
            }
        });
    }}
