package com.SmartScan.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.SmartScan.R;
import com.SmartScan.Tables.Item;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
    private List<Item> itemList;

    public ItemAdapter(List<Item> itemList) {
        this.itemList = itemList;
    }

    public void updateData(List<Item> newItemList) {
        this.itemList = newItemList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = itemList.get(position);
        holder.tvItemBarCode.setText(item.getItemBarCode());
        holder.tvItemDesc.setText(item.getItemDesc());
        holder.tvRemark.setText(item.getRemark());
        holder.tvOpt3.setText(item.getOpt3());
        holder.tvStatus.setText(item.getStatus());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemBarCode, tvItemDesc, tvRemark, tvOpt3, tvStatus;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemBarCode = itemView.findViewById(R.id.tvItemBarCode);
            tvItemDesc = itemView.findViewById(R.id.tvItemDesc);
            tvRemark = itemView.findViewById(R.id.tvRemark);
            tvOpt3 = itemView.findViewById(R.id.tvOpt3);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}