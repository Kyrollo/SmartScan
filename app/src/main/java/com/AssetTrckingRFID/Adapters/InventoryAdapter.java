package com.AssetTrckingRFID.Adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.AssetTrckingRFID.R;
import com.AssetTrckingRFID.Tables.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.inventoryViewHolder> {
    private List<Inventory> inventoryList;

    public InventoryAdapter(List<Inventory> inventoryList) {
        this.inventoryList = inventoryList;
    }

    @NonNull
    @Override
    public inventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data, parent, false);
        return new inventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull inventoryViewHolder holder, int position) {
        Inventory item = inventoryList.get(position);
        holder.tvItemBarCode.setText(item.getItemBarcode());
        holder.tvItemDesc.setText(item.getCategoryDesc());
        holder.tvRemark.setText(item.getRemark());
        holder.tvOpt3.setText(item.getTagId());
    }

    @Override
    public int getItemCount() {
        return inventoryList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Inventory> newInventoryList) {
        this.inventoryList.clear();
        this.inventoryList.addAll(newInventoryList);
        notifyDataSetChanged();
    }

    public static class inventoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemBarCode, tvItemDesc, tvRemark, tvOpt3;

        public inventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemBarCode = itemView.findViewById(R.id.tvItemBarCode);
            tvItemDesc = itemView.findViewById(R.id.tvItemDesc);
            tvRemark = itemView.findViewById(R.id.tvRemark);
            tvOpt3 = itemView.findViewById(R.id.tvOpt3);
        }
    }
}