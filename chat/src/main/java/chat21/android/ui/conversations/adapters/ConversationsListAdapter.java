package chat21.android.ui.conversations.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.vanniktech.emoji.EmojiTextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import chat21.android.R;
import chat21.android.core.conversations.models.Conversation;
import chat21.android.core.messages.models.Message;
import chat21.android.ui.adapters.AbstractRecyclerAdapter;
import chat21.android.ui.conversations.listeners.OnConversationClickListener;
import chat21.android.ui.conversations.listeners.OnConversationLongClickListener;
import chat21.android.utils.StringUtils;
import chat21.android.utils.TimeUtils;
import chat21.android.utils.image.CropCircleTransformation;

/**
 * Created by stefanodp91 on 18/12/17.
 */
public class ConversationsListAdapter extends AbstractRecyclerAdapter<Conversation, ConversationsListAdapter.ViewHolder> {
    private static final String TAG = ConversationsListAdapter.class.getName();

    private OnConversationClickListener onConversationClickListener;
    private OnConversationLongClickListener onConversationLongClickListener;

    public OnConversationClickListener getOnConversationClickListener() {
        return onConversationClickListener;
    }

    public void setOnConversationClickListener(OnConversationClickListener onConversationClickListener) {
        this.onConversationClickListener = onConversationClickListener;
    }

    public OnConversationLongClickListener getOnConversationLongClickListener() {
        return onConversationLongClickListener;
    }

    public void setOnConversationLongClickListener(OnConversationLongClickListener onConversationLongClickListener) {
        this.onConversationLongClickListener = onConversationLongClickListener;
    }

    public ConversationsListAdapter(Context context, List<Conversation> conversations) {
        super(context, conversations);
        sortByTimestamp(conversations);
    }

    @Override
    public void setList(List<Conversation> mList) {
        super.setList(mList);
        sortByTimestamp(mList);
    }

    private void sortByTimestamp(List<Conversation> conversations) {
        Collections.sort(conversations, new Comparator<Conversation>() {
            @Override
            public int compare(Conversation i1, Conversation i2) {
                return i1.getTimestampLong().compareTo(i2.getTimestampLong());
            }
        });
    }

    @Override
    public ConversationsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ConversationsListAdapter.ViewHolder holder, final int position) {
        final Conversation conversation = getItem(position);

        // TODO: 19/12/17 retrieve the pictrure url from the recipient
        setRecipientPicture(holder, conversation, "");

        setRecipientDisplayName(holder, conversation.getConvers_with_fullname(), conversation.getConvers_with());

        setLastMessageText(holder, conversation.getIs_new(), conversation.getLast_message_text());

        setTimestamp(holder, conversation.getIs_new(), conversation.getTimestampLong());

        setConversationCLickAction(holder, conversation, position);

        setConversationLongCLickAction(holder, conversation, position);
    }

    private void setRecipientPicture(ViewHolder holder, Conversation conversation, String pictureUrl) {
        if (conversation.getChannelType() == Message.DIRECT_CHANNEL_TYPE) {
            Glide.with(holder.itemView.getContext())
                    .load(pictureUrl)
                    .placeholder(R.drawable.ic_person_avatar)
                    .bitmapTransform(new CropCircleTransformation(holder.itemView.getContext()))
                    .into(holder.recipientPicture);
        } else if (conversation.getChannelType() == Message.GROUP_CHANNEL_TYPE) {
            Glide.with(holder.itemView.getContext())
                    .load(pictureUrl)
                    .placeholder(R.drawable.ic_group_avatar)
                    .bitmapTransform(new CropCircleTransformation(holder.itemView.getContext()))
                    .into(holder.recipientPicture);
        } else {
            Toast.makeText(holder.itemView.getContext(), "channel type is undefined", Toast.LENGTH_SHORT).show();
        }
    }

    // set the recipient display name whom are talking with
    private void setRecipientDisplayName(ViewHolder holder, String recipientFullName, String recipientId) {
        String displayName = StringUtils.isValid(recipientFullName) ? recipientFullName : recipientId;
        holder.recipientDisplayName.setText(displayName);
    }

    // show te last message text
    private void setLastMessageText(ViewHolder holder, boolean hasNewMessages, String lastMessageText) {
        // TODO: 18/12/17 format the text using the person who sent the message as prefix - check if the conversWith is a group or a person

        if (hasNewMessages) {
            // show bold text
            holder.lastTextMessage.setText(Html.fromHtml("<b>" + lastMessageText + "</b>"));
        } else {
            // not not bold text
            holder.lastTextMessage.setText(lastMessageText);
        }
    }

    // show the last sent message timestamp
    private void setTimestamp(ViewHolder holder, boolean hasNewMessages, long timestamp) {
        // format the timestamp to a pretty visible format
        String formattedTimestamp = TimeUtils.getFormattedTimestamp(timestamp);

        if (hasNewMessages) {
            // show bold text
            holder.lastMessageTimestamp.setText(Html.fromHtml("<b>" + formattedTimestamp + "</b>"));
        } else {
            // not not bold text
            holder.lastMessageTimestamp.setText(formattedTimestamp);
        }
    }

    // set on row click listener
    private void setConversationCLickAction(ViewHolder holder, final Conversation conversation, final int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getOnConversationClickListener() != null) {
                    getOnConversationClickListener().onConversationClicked(conversation, position);
                } else {
                    Log.w(TAG, "ConversationsListAdapter.setConversationCLickAction: getOnConversationClickListener() is null. " +
                            "set it with setOnConversationClickListener method. ");
                }
            }
        });
    }

    // set on row long click listener
    private void setConversationLongCLickAction(ViewHolder holder, final Conversation conversation, final int position) {
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (getOnConversationLongClickListener() != null) {
                    getOnConversationLongClickListener().onConversationLongClicked(conversation, position);

                    // source :
                    // https://stackoverflow.com/questions/18911290/perform-both-the-normal-click-and-long-click-at-button
                    return true; // event triggered
                } else {
                    Log.w(TAG, "ConversationsListAdapter.setConversationLongCLickAction: getOnConversationLongClickListener is null. " +
                            "set it with setOnConversationLongClickListener method. ");
                }

                return false; // event not triggered
            }
        });
    }

//    /**
//     * Update an item with a new value
//     *
//     * @param item the item
//     */
//    @Override
//    public void update(Conversation item) {
//        if (item != null) {
//            List<Conversation> list = getItems();
//
//            int itemPosition = list.indexOf(item);
//
//            if (itemPosition >= 0 && itemPosition < list.size()) {
//                list.remove(itemPosition); // remove from its position
//                list.add(0, item); // add on top
//            } else {
//                list.add(0, item); // add on top
//            }
//            notifyDataSetChanged();
//        }
//    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView recipientPicture;
        private TextView recipientDisplayName;
        private EmojiTextView lastTextMessage;
        private TextView lastMessageTimestamp;

        public ViewHolder(View itemView) {
            super(itemView);

            recipientPicture = itemView.findViewById(R.id.recipient_picture);
            recipientDisplayName = itemView.findViewById(R.id.recipient_display_name);
            lastTextMessage = itemView.findViewById(R.id.last_text_message);
            lastMessageTimestamp = itemView.findViewById(R.id.last_message_timestamp);
        }
    }
}