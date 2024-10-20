package com.AssetTrckingRFID.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.AssetTrckingRFID.Assign.Tag;
import com.AssetTrckingRFID.R;

import java.util.List;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ViewHolder> {
    private List<Tag> tagList;
    private OnItemLongClickListener longClickListener;

    public interface OnItemLongClickListener {
        void onItemLongClick(Tag tag);
    }

    public TagAdapter(List<Tag> tagList, OnItemLongClickListener longClickListener) {
        this.tagList = tagList;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tag tag = tagList.get(position);
        holder.bind(tag, longClickListener);
    }

    @Override
    public int getItemCount() {
        return tagList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView barcodeTextView;
        private TextView rfidTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            barcodeTextView = itemView.findViewById(R.id.itemBarcode);
            rfidTextView = itemView.findViewById(R.id.tagId);
        }

        public void bind(final Tag tag, final OnItemLongClickListener longClickListener) {
            barcodeTextView.setText(tag.getItemBarcode());
            rfidTextView.setText(tag.getTagId());

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    longClickListener.onItemLongClick(tag);
                    return true;
                }
            });
        }
    }
}