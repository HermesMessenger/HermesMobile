package org.hermesmessenger.hermes;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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

    public List<Message> getMessages() {
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
    public View getView(int i, View convertView, ViewGroup viewGroup) {

        if (convertView == null) {

            MessageViewHolder holder = new MessageViewHolder();
            LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            Message message = messages.get(i);

            if (message.belongsToCurrentUser()) { // this message was sent by us so let's create a basic chat bubble on the right
                convertView = messageInflater.inflate(R.layout.my_message, null);
                holder.messageBody = convertView.findViewById(R.id.message);
                convertView.setTag(holder);
                holder.messageBody.setText(message.getMessage());

            } else { // this message was sent by someone else so let's create an advanced chat bubble on the left

                UserAdapter userAdapter = new UserAdapter();
                User user = userAdapter.getUser(message.getSender());

                convertView = messageInflater.inflate(R.layout.their_message, null);
                convertView.setTag(holder);

                holder.avatar = convertView.findViewById(R.id.image);
                holder.name = convertView.findViewById(R.id.username);
                holder.messageBody = convertView.findViewById(R.id.message);

                holder.name.setText(message.getSender());
             //   holder.name.setTextColor(Color.parseColor(user.getColor()));
                holder.messageBody.setText(message.getMessage());

               // holder.avatar.setImageBitmap(user.getImage_bitmap());

            }
        }

        return convertView;
    }

}

class MessageViewHolder {
    public ImageView avatar;
    public TextView name;
    public TextView messageBody;
}
