package com.example.asus.ocrreaderwithtxttospeech.utilities;



import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.ocrreaderwithtxttospeech.MainActivity;
import com.example.asus.ocrreaderwithtxttospeech.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class OChist extends AppCompatActivity implements SearchView.OnQueryTextListener {
    AsyncHttpClient client;
    RequestParams params;
    AsyncHttpClient newclient;
    RequestParams newparams;
    ArrayList<String> details;
    ArrayList<ContentTitle> titles;
    ArrayList<ContentTitle>orig;
    EditText pin;
    String itemvalue;
    private SearchView mSearchView;
//    AsyncHttpClient client1;
//    RequestParams params1;
//    JSONObject objpin;
//   String pinurl="http://srishti-systems.info/projects/ocr/pin.php?id=1&password=1234";

    ListView listview;
    LayoutInflater inflate;

    String url="http://srishti-systems.info/projects/ocr/view_string.php?";
    String newurl="http://srishti-systems.info/projects/ocr/pin.php?id=1&password=1234";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocrhist);

        listview = findViewById(R.id.lview);
        mSearchView = (SearchView) findViewById(R.id.searchv);
        client = new AsyncHttpClient();
        params = new RequestParams();
        newclient = new AsyncHttpClient();
        newparams = new RequestParams();

        SharedPreferences sp = getApplicationContext()
                .getSharedPreferences("d1", MODE_PRIVATE);
        final String stid = sp.getString("did", null);
        details = new ArrayList<>();
        orig = new ArrayList<>();
        titles = new ArrayList<>();

        Log.e("In", "ouut");

        params.put("id", stid);

        client.get(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String content) {
                super.onSuccess(content);
                try {
                    Log.e("Inn", "out");
                    JSONObject jobjmain = new JSONObject(content);
                    if (jobjmain.getString("status").equals("success")) {

                        JSONArray jarray = jobjmain.getJSONArray("String_details");
                        for (int i = 0; i < jarray.length(); i++) {
                            JSONObject jobj = jarray.getJSONObject(i);
                            String st = jobj.getString("string");

                            String ptitle = jobj.getString("title");
                            titles.add(new ContentTitle(ptitle, st));


                        }
                    }

                    adapter adp = new adapter(OChist.this, titles);
                    listview.setAdapter(adp);
                    listview.setTextFilterEnabled(true);
                    listview.setTextFilterEnabled(true);
                    setupSearchView();

//                    setupSearchView();

                } catch (Exception e) {

                }
                listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {


                        int ItemPosition = position;


                         itemvalue=titles.get(position).getDetails();
                        LayoutInflater inflat=LayoutInflater.from(OChist.this);
                        View cuslay=inflat.inflate(R.layout.askpin,null);

                         pin=cuslay.findViewById(R.id.epin);
                        Button ok=cuslay.findViewById(R.id.eok);
                        AlertDialog.Builder AB=new AlertDialog.Builder(OChist.this);
                        AB.setView(cuslay);
                        final AlertDialog A=AB.create();
                        A.show();
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        newparams.put("id", stid);
                        newparams.put("password", pin.getText().toString());

                        newclient.get(newurl, newparams, new AsyncHttpResponseHandler() {

                            @Override
                            public void onSuccess(String content) {
                                super.onSuccess(content);

                                try {
                                    Log.e("innn", "in");

                                    JSONObject newobj = new JSONObject(content);

                                    String s = newobj.getString("status");

                                    Toast.makeText(OChist.this, "" + s, Toast.LENGTH_SHORT).show();
                                    if (s.equals("Success")) {


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


                                } catch (Exception e) {

                                }
                            }
                        });

                    }
                });





                    }
                });


            }
        });


    }

    public class adapter extends BaseAdapter implements Filterable {

        public Context context;
        public ArrayList<ContentTitle> titles;
        public ArrayList<ContentTitle> orig;

        public adapter(Context context, ArrayList<ContentTitle> titles) {
            super();
            this.context = context;
            this.titles = titles;
        }


        public class EmployeeHolder
        {
            TextView texttitle;
            TextView textdetails;
        }

        public Filter getFilter() {
            return new Filter() {

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    final FilterResults oReturn = new FilterResults();
                    final ArrayList<ContentTitle> results = new ArrayList<ContentTitle>();
                    if (orig == null)
                        orig = titles;
                    if (constraint != null) {
                        if (orig != null && orig.size() > 0) {
                            for (final ContentTitle g : orig) {
                                if (g.getTitle().toLowerCase()
                                        .contains(constraint.toString()))
                                    results.add(g);
                            }
                        }
                        oReturn.values = results;
                    }
                    return oReturn;
                }

                @SuppressWarnings("unchecked")
                @Override
                protected void publishResults(CharSequence constraint,
                                              FilterResults results) {
                   titles = (ArrayList<ContentTitle>) results.values;
                    notifyDataSetChanged();
                }
            };
        }

        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }


        @Override
        public int getCount() {
            return titles.size();
        }

        @Override
        public Object getItem(int position) {
            return titles.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            EmployeeHolder holder;
            if(convertView==null)
            {
                convertView=LayoutInflater.from(context).inflate(R.layout.historylview, parent, false);
                holder=new EmployeeHolder();
                holder.texttitle=(TextView) convertView.findViewById(R.id.titleid);
                holder.textdetails=(TextView) convertView.findViewById(R.id.textview);
                convertView.setTag(holder);
            }
            else
            {
                holder=(EmployeeHolder) convertView.getTag();
            }


            holder.texttitle.setText(titles.get(position).getTitle());
            holder.textdetails.setText(String.valueOf(titles.get(position).getDetails()));
//            convertView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Toast.makeText(getApplicationContext(),titles.get(position).getDetails(),Toast.LENGTH_LONG).show();
//                }
//            });
            return convertView;

        }

    }




    private void setupSearchView()
    {
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setSubmitButtonEnabled(true);
        mSearchView.setQueryHint("Search Here");
    }

    @Override
    public boolean onQueryTextChange(String newText)
    {

        if (TextUtils.isEmpty(newText)) {
            listview.clearTextFilter();
        } else {
            listview.setFilterText(newText);
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query)
    {
        return false;
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }



}
