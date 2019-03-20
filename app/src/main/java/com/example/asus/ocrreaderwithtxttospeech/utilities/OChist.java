package com.example.asus.ocrreaderwithtxttospeech.utilities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.example.asus.ocrreaderwithtxttospeech.MainActivity;
import com.example.asus.ocrreaderwithtxttospeech.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class OChist extends AppCompatActivity {
    AsyncHttpClient client;
    RequestParams params;

    ArrayList<String> details;
    ArrayList<String> titles;
    ArrayList<String>string;
    SearchView searchView;
    ListView listview;
    LayoutInflater inflate;

    String url="http://srishti-systems.info/projects/ocr/view_string.php?";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocrhist);

        listview=findViewById(R.id.lview);
        searchView=findViewById(R.id.search);
        client=new AsyncHttpClient();
        params=new RequestParams();
        SharedPreferences sp=getApplicationContext()
                .getSharedPreferences("d1",MODE_PRIVATE);
        final String stid=sp.getString("did",null);
        details=new ArrayList<>();
        titles=new ArrayList<>();

        Log.e ("In","ouut");

        params.put("id",stid);

        client.get(url,params,new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String content) {
                super.onSuccess(content);
                try {
                    Log.e("Inn","out");
                    JSONObject jobjmain=new JSONObject(content);
                    if (jobjmain.getString("status").equals("success")){

                        JSONArray jarray=jobjmain.getJSONArray("String_details");
                        for(int i=0;i<jarray.length();i++){
                            JSONObject jobj=jarray.getJSONObject(i);
                            String st=jobj.getString("string");
                            details.add(""+st);
                            String ptitle=jobj.getString("title");
                            titles.add(""+ptitle);

                        }
                    }

                    adapter adp=new adapter();
                    listview.setAdapter(adp);
                   listview.setTextFilterEnabled(true);
//                    setupSearchView();

                }catch (Exception e){

                }
            }
        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {


                 int ItemPosition = position;

                 String itemvalue=details.get(position);
                LayoutInflater inflat=LayoutInflater.from(OChist.this);
                View cuslay=inflat.inflate(R.layout.askpin,null);


                EditText pin=cuslay.findViewById(R.id.epin);
                Button ok=cuslay.findViewById(R.id.eok);
                AlertDialog.Builder AB=new AlertDialog.Builder(OChist.this);
                AB.setView(cuslay);
                final AlertDialog A=AB.create();
                A.show();
//                .setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent i=new Intent(MainActivity.this,HospitalRegistration.class);
//                        startActivity(i);
//                    }
//                });
                String encrypted = itemvalue;
                String decrypted = "";
                try {
                    decrypted = AESUtils.decrypt(encrypted);
                    Log.d("TEST", "decrypted:" + decrypted);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Intent myintent=new Intent(OChist.this, MainActivity.class).putExtra("key1", decrypted
                );
                startActivity(myintent);


            }
        });


    }


    class adapter extends BaseAdapter {

        @Override
        public int getCount() {

            return details.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            inflate=(LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView=inflate.inflate(R.layout.historylview,null);
            TextView ttitle=convertView.findViewById(R.id.titleid);
            TextView tview=convertView.findViewById(R.id.textview);
            ttitle.setText(titles.get(position));
            tview.setText(details.get(position));
            return convertView;
        }

    }


}

