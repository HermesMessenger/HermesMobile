package org.hermesmessenger.hermes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.ParsedRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static org.hermesmessenger.hermes.Settings.HermesURL;
import static org.hermesmessenger.hermes.Settings.HermesUUID;
import static org.hermesmessenger.hermes.Settings.HermesUsername;

public class Chat extends AppCompatActivity {

    private static class MessageStyleData {
        public Bitmap avatar;
        public String color;

        public MessageStyleData(Bitmap avatar, String color) {
            this.avatar = avatar;
            this.color = color;
        }
    }

    MessageAdapter messageAdapter;
    int offset = 0;

    Map<String, MessageStyleData> user_styles; // TODO Save in images as paths to files, not bitmaps, because it causes an out of memory error

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        new Settings(this);

        if (HermesUUID.equals("")) {
            startActivity(new Intent(this, Login.class));
            return;

        } else setContentView(R.layout.activity_chat);

        AndroidNetworking.initialize(getApplicationContext());

        messageAdapter = new MessageAdapter(Chat.this);
        ListView messagesListView  = (ListView) findViewById(R.id.messages_view);
        messagesListView.setAdapter(messageAdapter);

        try {
            File file = new File(getFilesDir(), "messageCache.txt");
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            List<Message> messages = (List<Message>) ois.readObject();
            ois.close();

            messageAdapter.loadFromCache(messages);

        } catch (Exception err) {
            Log.e("Cache Error", err.toString());
        }

        Timer t = new Timer();
        t.schedule(new TimerTask() {

            @Override
            public void run() {
                Chat.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        loadMessages();

                        List messages = messageAdapter.getMessages();

                        try {

                            File file = new File(getFilesDir(), "messageCache.txt");
                            FileOutputStream fos = new FileOutputStream(file);
                            ObjectOutputStream oos = new ObjectOutputStream(fos);
                            oos.writeObject(messages);
                            oos.close();

                        } catch (IOException err) {
                            Log.e("Cache Error", err.toString());
                        }
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

        } else AndroidNetworking.post(HermesURL + "/api/sendmessage/")
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

    String sender = "";
    String text = "";
    String time = "";
    boolean belongsToCurrentUser = false;
    String color = "#000000";
    Bitmap avatar = null;

    public void loadMessages() {

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("preferences", 0);
        final int offset = sharedPref.getInt("Offset", 0);

        AndroidNetworking.post(HermesURL + "/api/loadmessages/" + offset)
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

                            if(json.getInt("time") > offset) {
                                sender = json.getString("username");
                                text = json.getString("message");
                                time = json.getString("time");
                                belongsToCurrentUser = sender.equals(HermesUsername);          final boolean belongsToMe = sender.equals(HermesUsername);

                                if (!user_styles.containsKey(sender)) {
                                    AndroidNetworking.get(HermesURL + "/api/getSettings/" + sender).build().getAsJSONObject(new JSONObjectRequestListener() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try {
                                                avatar = Utils.getB64Image(response.getString("image"));
                                                color = response.getString("color");
                                                user_styles.put(sender, new MessageStyleData(avatar, color));
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        messageAdapter.add(new Message(sender, text, time, belongsToCurrentUser, avatar, color));
                                                    }
                                                });
                                            } catch (JSONException e) {
                                                Log.e("Error", "JSON error: " + e);
                                            }
                                        }

                                        @Override
                                        public void onError(ANError anError) {
                                            Log.e("Error", "Request error: " + anError);
                                        }
                                    });
                                } else {
                                    MessageStyleData nonNullStyle = (MessageStyleData) Objects.requireNonNull(user_styles.get(sender));
                                    color = nonNullStyle.color;
                                    avatar = nonNullStyle.avatar;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            messageAdapter.add(new Message(sender, text, time, belongsToCurrentUser, avatar, color));
                                        }
                                    });
                                }


                                editor.putInt("Offset", json.getInt("time"));
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


