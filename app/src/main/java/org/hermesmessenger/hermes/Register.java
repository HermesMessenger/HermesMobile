package org.hermesmessenger.hermes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;


public class Register extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        String password = intent.getStringExtra("password");
        TextView nameView = findViewById(R.id.username);
        TextView passView = findViewById(R.id.password);
        nameView.setText(username);
        passView.setText(password);

        AndroidNetworking.initialize(getApplicationContext());

    }

    public void register(View view) {
        EditText userText = (EditText) findViewById(R.id.username);
        EditText passText = (EditText) findViewById(R.id.password);
        EditText pass2Text= (EditText) findViewById(R.id.repeatPassword);
        String username = userText.getText().toString();
        String password = passText.getText().toString();
        String password2 = pass2Text.getText().toString();

        if (username.matches("")) {
            Toast.makeText(this, "Please enter your username", Toast.LENGTH_SHORT).show();

        } else if (password.matches("")) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();

        } else if (password2.matches("")) {
            Toast.makeText(this, "Please repeat your password", Toast.LENGTH_SHORT).show();

        } else if (!(password.equals(password2))) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();

        } else AndroidNetworking.post("https://hermesmessenger-testing.duckdns.org/api/register")
            .addBodyParameter("username", username)
            .addBodyParameter("password1", password)
            .addBodyParameter("password2", password2)
            .setPriority(Priority.HIGH)
            .build()
            .getAsString(new StringRequestListener() {
                @Override
                public void onResponse(String res) {

                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("preferences", 0);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("UUID", res);
                    editor.commit();

                    startActivity(new Intent(Register.this, Chat.class)); // Go to chat

                }

                @Override
                public void onError(ANError err) {
                    Toast.makeText(Register.this, "User already exists", Toast.LENGTH_SHORT).show();
                }
            });
    }

    public void goToLogin(View view) {
        Intent intent = new Intent(this, Login.class);
        EditText userText = (EditText) findViewById(R.id.username);
        EditText passText = (EditText) findViewById(R.id.password);
        String username = userText.getText().toString();
        String password = passText.getText().toString();
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        startActivity(intent);
    }

}
