package org.hermesmessenger.hermes;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
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
import com.androidnetworking.interfaces.StringRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import static org.hermesmessenger.hermes.Settings.HermesURL;
import static org.hermesmessenger.hermes.Settings.HermesUUID;
import static org.hermesmessenger.hermes.Settings.HermesUsername;

public class Chat extends AppCompatActivity implements Serializable {

    UserAdapter userAdapter = new UserAdapter();
    MessageAdapter messageAdapter = new MessageAdapter(Chat.this);
    Utils utils = new Utils();
    int notificationID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        new Settings(this);

        if (HermesUUID.equals("")) {
            startActivity(new Intent(this, Login.class));
            return;

        } else setContentView(R.layout.activity_chat);

        AndroidNetworking.initialize(getApplicationContext());

        ListView messagesListView = findViewById(R.id.messages_view);
        messagesListView.setAdapter(messageAdapter);

        try {
            Log.d("Cache Read", "Reading cache...");
            File messageFile = new File(getFilesDir(), "messageCache.txt");
            File userFile = new File(getFilesDir(), "userCache.txt");
            FileInputStream messagefis = new FileInputStream(messageFile);
            FileInputStream userfis = new FileInputStream(userFile);
            ObjectInputStream messageois = new ObjectInputStream(messagefis);
            ObjectInputStream userois = new ObjectInputStream(userfis);
            List<Message> messages = (List<Message>) messageois.readObject();
            HashMap<String, User> users = (HashMap<String, User>) userois.readObject();
            messageois.close();
            userois.close();

            messageAdapter.loadFromCache(messages);
            userAdapter.loadFromCache(users);

        } catch (Exception err) {
            Log.e("Cache Read Error", err.toString());
        }

        utils.setTimeout(this::loadMessages, 500);
        utils.setTimeout(this::writeCache, 1000 * 60 *10); // 10 minutes

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

                    SharedPreferences.Editor editor = sharedPref.edit();

                    for(int n = 0; n < res.length(); n++) {

                        try {
                            JSONObject json = res.getJSONObject(n);

                            final String sender = json.getString("username");
                            final String text = json.getString("message");
                            final String time = json.getString("time");
                            final boolean belongsToCurrentUser = sender.equals(HermesUsername);

                            if (! userAdapter.containsKey(sender)) {

                                AndroidNetworking.get(HermesURL + "/api/getSettings/" + sender).build().getAsJSONObject(new JSONObjectRequestListener() {
                                    @Override
                                    public void onResponse(JSONObject res) {
                                        try {
                                            final String color = res.getString("color");
                                            final String image = res.getString("image");


                                            runOnUiThread(() -> userAdapter.add(sender, new User(color, image)));

                                        } catch (JSONException err) {
                                            Log.e("JSON error", err.toString());
                                        }
                                    }

                                    @Override
                                    public void onError(ANError err) {
                                        Log.e("HTTP error", err.toString());
                                    }

                                });
                            }

                            Message message = new Message(sender, text, time, belongsToCurrentUser);

                            runOnUiThread(() -> messageAdapter.add(message));

                            if (!(Foreground.get().isForeground()))  showNotification(message);

                            editor.putInt("Offset", json.getInt("time"));

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

        if (Foreground.get().isForeground()) {
            utils.setTimeout(this::loadMessages, 200);

        } else {
            utils.setTimeout(this::loadMessages, 20000);
        }

    }

    public void writeCache () {
        try {

            File messageFile = new File(getFilesDir(), "messageCache.txt");
            File userFile = new File(getFilesDir(), "userCache.txt");

            FileOutputStream messagefos = new FileOutputStream(messageFile);
            FileOutputStream userfos = new FileOutputStream(userFile);
            ObjectOutputStream messageoos = new ObjectOutputStream(messagefos);
            ObjectOutputStream useroos = new ObjectOutputStream(userfos);

            List<Message> messages = messageAdapter.getMessages();
            HashMap<String, User> users = userAdapter.getUsers();

            messageoos.writeObject(messages);
            useroos.writeObject(users);
            messageoos.close();
            useroos.close();

        } catch (Exception err) {
            Log.e("Cache Write Error", err.toString());
        }

    }

    public void showNotification(Message message) {

        Log.d("Notifications", "Showing notification...");

        Intent intent = new Intent(this, Chat.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "Messages")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("New message from " + message.getSender())
                .setContentText(message.getSender() + ": " + message.getMessage())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message.getSender() + ": " + message.getMessage()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationID, mBuilder.build());

        notificationID++;

    }
}


