package org.hermesmessenger.hermes;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends BaseAdapter implements Serializable {

    List<Message> messages = new ArrayList<>();
    Context context;

    public MessageAdapter(Context context) {
        this.context = context;
    }

    public void add(Message message) {
        this.messages.add(message);
        this.notifyDataSetChanged();
    }

    public void loadFromCache(List<Message> messages ) {
        this.messages = messages;
    }

    public List getMessages() {
        return messages;
    }


    @Override
    public Object getItem(int i) {
        return messages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        MessageViewHolder holder = new MessageViewHolder();
        LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        Message message = messages.get(i);

        if (message.belongsToCurrentUser()) { // message was sent by current user
            view = messageInflater.inflate(R.layout.my_message, null);
            holder.messageBody = view.findViewById(R.id.message);
            view.setTag(holder);
            holder.messageBody.setText(message.getMessage());

        } else { // message was sent by someone else
            view = messageInflater.inflate(R.layout.their_message, null);
            holder.avatar = view.findViewById(R.id.image);
            holder.name = view.findViewById(R.id.username);
            holder.messageBody = view.findViewById(R.id.message);
            view.setTag(holder);

            // TODO: Avatar (https://stackoverflow.com/questions/2471935/how-to-load-an-imageview-by-url-in-android), we would need an imageview, not a view

            holder.name.setText(message.getSender());
            holder.messageBody.setText(message.getMessage());
            ColorDrawable drawable = (ColorDrawable) holder.avatar.getBackground();

            drawable.setColor(Color.parseColor("#81D4FA"));
        }
        return view;
    }

}

class MessageViewHolder {
    public View avatar;
    public TextView name;
    public TextView messageBody;
}
