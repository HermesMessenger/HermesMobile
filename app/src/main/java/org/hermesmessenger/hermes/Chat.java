package org.hermesmessenger.hermes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.ParsedRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class Chat extends AppCompatActivity {

    static String HermesURL = "https://hermesmessenger-testing.duckdns.org";
    String HermesUUID;
    String HermesUsername;

    MessageAdapter messageAdapter;
    int offset = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        Settings settings = new Settings(this);


        HermesUUID = settings.getUUID();
        HermesUsername = settings.getUsername();

        if (HermesUUID.equals("")) {
            startActivity(new Intent(this, Login.class));
            return;

        } else setContentView(R.layout.activity_chat);

        AndroidNetworking.initialize(getApplicationContext());

        messageAdapter = new MessageAdapter(Chat.this);
        ListView messagesListView  = (ListView) findViewById(R.id.messages_view);
        messagesListView.setAdapter(messageAdapter);

        Timer t = new Timer();
        t.schedule(new TimerTask() {

            @Override
            public void run() {
                Chat.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        loadMessages();
                    }
                });
            }
        },0, 500);

    }


    public void sendMessage(View view) {
        final EditText msg = findViewById(R.id.msg);
        final String message = msg.getText().toString();

        if (message.matches("^\\s*$")) {
            Toast.makeText(this, "Message is empty", Toast.LENGTH_SHORT).show();

        } else AndroidNetworking.post("https://hermesmessenger-testing.duckdns.org/api/sendmessage/")
            .addBodyParameter("uuid", HermesUUID)
            .addBodyParameter("message", message)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsString(new StringRequestListener() {
                @Override
                public void onResponse(String res) {
                    // No need to do anything with the response since the message was sent successfully
                }

                @Override
                public void onError(ANError err) {
                    Toast.makeText(Chat.this, "Couldn't send message.", Toast.LENGTH_SHORT).show();
                }
            });

        msg.setText(""); // Clear message field
    }

    public void loadMessages() {

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("preferences", 0);
        //int offset = sharedPref.getInt("Offset", 0);
        AndroidNetworking.post("https://hermesmessenger-testing.duckdns.org/api/loadmessages/" + offset)
            .addBodyParameter("uuid", HermesUUID)
            .setPriority(Priority.LOW)
            .build()
            .getAsJSONArray(new JSONArrayRequestListener() {
                @Override
                public void onResponse(JSONArray res) {

                    SharedPreferences sharedPref = Chat.this.getSharedPreferences("preferences", 0);
                    SharedPreferences.Editor editor = sharedPref.edit();

                    for(int n = 0; n < res.length(); n++) {

                        try {
                            JSONObject json = res.getJSONObject(n);

                            if(json.getInt("time")>offset) {
                                final String sender = json.getString("username");
                                final String text = json.getString("message");
                                final String time = json.getString("time");
                                final boolean belongsToMe = sender.equals(HermesUsername);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        messageAdapter.add(new Message(sender, text, time, belongsToMe));
                                    }
                                });

                                offset = json.getInt("time");
                            }

                        } catch (JSONException err) {
                            Log.e("JSON error", err.toString());
                        }
                    }

                    editor.commit();
                }
                @Override
                public void onError(ANError err) {
                    Log.e("HTTP error", err.toString());
                }
            });
    }

};


