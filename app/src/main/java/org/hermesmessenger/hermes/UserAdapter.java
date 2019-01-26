package org.hermesmessenger.hermes;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.io.Serializable;
import java.util.HashMap;

public class UserAdapter extends BaseAdapter implements Serializable {

    static HashMap<String, User> users = new HashMap<String, User>(); // TODO Save in images as paths to files, not bitmaps, because it causes an out of memory error

    public void add(String user, User settings) {
        users.put(user, settings);
    }

    public boolean containsKey(String user) {
        return users.containsKey(user);
    }

    public User getUser(String name) {
        Log.d("Users", users.toString());
        return users.get(name);
    }

    public void loadFromCache(HashMap<String, User> user ) {
        users = user;
    }

    public HashMap<String, User> getUsers() {
        return users;
    }


    @Override
    public Object getItem(int i) {
        return users.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    @Override
    public int getCount() {
        return users.size();
    }

}

