package com.SmartScan.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.SmartScan.R;

import java.util.ArrayList;
import java.util.List;

public class AssetAdapter extends RecyclerView.Adapter<AssetAdapter.AssetViewHolder> {
    private List<AssetInfo> assetList;

    public AssetAdapter(List<AssetInfo> assetList) {
        this.assetList = assetList != null ? assetList : new ArrayList<>();
    }

    @NonNull
    @Override
    public AssetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data, parent, false);
        return new AssetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssetViewHolder holder, int position) {
        AssetInfo asset = assetList.get(position);
        holder.tvItemBarCode.setText(asset.getItemBarCode());
        holder.tvItemDesc.setText(asset.getItemDesc());
        holder.tvRemark.setText(asset.getRemark());
        holder.tvOpt3.setText(asset.getOpt3());
    }

    @Override
    public int getItemCount() {
        return assetList.size();
    }

    public void updateData(List<AssetInfo> assetList) {
        this.assetList = assetList != null ? assetList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class AssetViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemBarCode, tvItemDesc, tvRemark, tvOpt3;

        public AssetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemBarCode = itemView.findViewById(R.id.tvItemBarCode);
            tvItemDesc = itemView.findViewById(R.id.tvItemDesc);
            tvRemark = itemView.findViewById(R.id.tvRemark);
            tvOpt3 = itemView.findViewById(R.id.tvOpt3);
        }
    }
}