package com.AssetTrckingRFID.Utilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

import com.AssetTrckingRFID.R;

public class LoadingDialog {
    Activity activity;
    AlertDialog dialog;
    public int flag = 0;

    public LoadingDialog(Activity myActivity)
    {
        activity = myActivity;
    }

    public void startLoadingDialog(){
        if(flag == 0 && isActivityValid()) {
            flag++;
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            LayoutInflater inflater = activity.getLayoutInflater();
            builder.setView(inflater.inflate(R.layout.custome_dialog, null));
            builder.setCancelable(false);
            dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
    }

    public void dismissDialog(){
        if(flag == 1) {
            flag--;
            if (dialog != null && dialog.isShowing() && isActivityValid()) {
                dialog.dismiss();
            }
        }
    }

    private boolean isActivityValid() {
        return activity != null && !activity.isFinishing() && !activity.isDestroyed();
    }
}