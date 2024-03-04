package tech.c1ph3rj.view.ChatScreen.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;

import org.w3c.dom.Text;

import java.io.File;
import java.time.chrono.HijrahChronology;
import java.util.ArrayList;

import tech.c1ph3rj.view.ChatScreen.ChatScreen;
import tech.c1ph3rj.view.ChatScreen.model.ChatMessages;
import tech.c1ph3rj.view.R;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private static final int LAYOUT_SENDER = 0;
    private static final int LAYOUT_RECEIVER = 1;
    Context context;
    ArrayList<ChatMessages> chatMessages;

    ChatScreen activity;
    OnLastItemVisibleListener listener;


    public ChatAdapter(ArrayList<ChatMessages> chatMessages, OnLastItemVisibleListener listener) {
        this.chatMessages = chatMessages;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessages message = chatMessages.get(position);
        if (message.isFromMyEnd()) {
            return LAYOUT_SENDER;
        } else {
            return LAYOUT_RECEIVER;
        }
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        this.activity = (ChatScreen) context;
        View view;
        if (viewType == LAYOUT_SENDER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_message_right, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_message_left, parent, false);
        }
        return new ChatViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, @SuppressLint("RecyclerView") int position) {
        try {
            ChatMessages message = chatMessages.get(position);
            if (getItemViewType(position) == LAYOUT_SENDER) {
                holder.bindSender(message);
            } else {
                holder.bindReceiver(message);
                if((position == chatMessages.size() - 1)) {
                    if(listener != null) {
                        listener.onLastItemVisible(message, holder.tvMessage, holder.tvMessageFrom);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvMessageFrom;
        ShimmerFrameLayout loadingView;
        CardView messageLayout;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.participantMessage);
            tvMessageFrom = itemView.findViewById(R.id.participantName);
            messageLayout = itemView.findViewById(R.id.messageCardView);
            loadingView = itemView.findViewById(R.id.loadingView);
        }

        public void bindSender(ChatMessages message) {
            if (!message.getMessage().trim().isEmpty() || !message.getMessage().equals("")) {
                tvMessage.setVisibility(View.VISIBLE);
                tvMessage.setText(message.getMessage());
            } else {
                tvMessage.setVisibility(View.GONE);
            }

            String userNameAndDate = message.getSenderName() + "  " + message.getDate();
            tvMessageFrom.setText(userNameAndDate);
        }

        public void bindReceiver(ChatMessages message) {
            if(message.isMsgLoading) {
                loadingView.setVisibility(View.VISIBLE);
                messageLayout.setVisibility(View.GONE);
            } else {
                loadingView.setVisibility(View.GONE);
                messageLayout.setVisibility(View.VISIBLE);
            }

            if (!message.getMessage().trim().isEmpty() || !message.getMessage().equals("")) {
                tvMessage.setVisibility(View.VISIBLE);
                tvMessage.setText(message.getMessage());
            } else {
                tvMessage.setVisibility(View.GONE);
            }

            String userNameAndDate = message.getSenderName() + "  " + message.getDate();
            tvMessageFrom.setText(userNameAndDate);
        }
    }

    public interface OnLastItemVisibleListener {
        void onLastItemVisible(ChatMessages message, TextView lastChatTextView, TextView senderDetailsView);
    }
}
