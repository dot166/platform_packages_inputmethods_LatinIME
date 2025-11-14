package com.android.inputmethod.keyboard.emoji;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.android.inputmethod.latin.R;

public class KaomojiAdapter
        extends RecyclerView.Adapter<KaomojiAdapter.Holder> {

    private String[] data = new String[0];
    private EmojiPageKeyboardView.OnKeyEventListener mListener;
    private int mTextColor = 0;

    public KaomojiAdapter(EmojiPageKeyboardView.OnKeyEventListener listener) {
        mListener = listener;
    }

    public void setData(String[] kaomojis) {
        data = kaomojis != null ? kaomojis : new String[0];
        notifyDataSetChanged();
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView tv = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.kaomoji_item, parent, false);
        return new Holder(tv);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        final String k = data[position];
        holder.text.setText(k);
        holder.text.setTextColor(mTextColor);
        holder.text.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onKaomojiPress(k);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.length;
    }

    public void setTextColor(int color) {
        if (mTextColor == color) return;
        mTextColor = color;
        notifyDataSetChanged();
    }

    public void addListener(EmojiPageKeyboardView.OnKeyEventListener listener) {
        mListener = listener;
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView text;
        Holder(TextView itemView) {
            super(itemView);
            text = itemView;
        }
    }
}

