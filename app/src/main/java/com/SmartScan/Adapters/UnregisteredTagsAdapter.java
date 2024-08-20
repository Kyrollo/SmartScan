package com.SmartScan.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.SmartScan.R;
import java.util.List;
import java.util.Set;

public class UnregisteredTagsAdapter extends RecyclerView.Adapter<UnregisteredTagsAdapter.ViewHolder> {
    private List<String> unregisteredTags;

    public UnregisteredTagsAdapter(Set<String> unregTags) {
        this.unregisteredTags = List.copyOf(unregTags);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_unregistered_tag, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String tag = unregisteredTags.get(position);
        holder.tagTextView.setText(tag);
    }

    @Override
    public int getItemCount() {
        return unregisteredTags.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tagTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            tagTextView = itemView.findViewById(R.id.tagTextView);
        }
    }
}