package com.example.quantrasuaclient.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.quantrasuaclient.Common.Common;
import com.example.quantrasuaclient.Model.ChatModel;
import com.example.quantrasuaclient.R;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter {
    private final ArrayList<ChatModel> chatModalsArrayList;
    private final Context context;

    public ChatAdapter(ArrayList<ChatModel> chatModalsArrayList, Context context) {
        this.chatModalsArrayList = chatModalsArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case 0:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_message_text, parent, false);
                return new UserViewHolder(view);
            case 1:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_message_bot, parent, false);
                return new BotViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatModel chatModal = chatModalsArrayList.get(position);
        switch (chatModal.getSender()) {
            case "user":
                if (Common.currentUser.getImage() != null) {
                    Glide.with(context).load(Common.currentUser.getImage()).into(((UserViewHolder) holder).UserImg);
                } else {
                    Glide.with(context).load(R.drawable.app_icon).into(((UserViewHolder) holder).UserImg);
                }
                ((UserViewHolder) holder).UserTV.setText(chatModal.getMessage());
                break;
            case "bot":
                Glide.with(context).load(R.drawable.chatbot).into(((BotViewHolder) holder).BotImg);
                ((BotViewHolder) holder).BotTV.setText(chatModal.getMessage());
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        switch (chatModalsArrayList.get(position).getSender()) {
            case "user":
                return 0;
            case "bot":
                return 1;
            default:
                return -1;
        }
    }

    @Override
    public int getItemCount() {
        return chatModalsArrayList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView UserImg;
        TextView UserTV;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            UserTV = itemView.findViewById(R.id.txt_chat_message);
            UserImg = itemView.findViewById(R.id.profile_image);
        }
    }

    public static class BotViewHolder extends RecyclerView.ViewHolder {
        ImageView BotImg;
        TextView BotTV;

        public BotViewHolder(@NonNull View itemView) {
            super(itemView);
            BotTV = itemView.findViewById(R.id.txt_chat_message);
            BotImg = itemView.findViewById(R.id.profile_image);
        }
    }
}