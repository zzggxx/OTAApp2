package com.snbc.otaapp;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

public class ToastUtils {

    private static Toast mToast;

    public static void showStringToast(Context context, int id) {
        if (mToast == null) {
            mToast = Toast.makeText(context, context.getString(id), Toast.LENGTH_SHORT);
        } else {
            View view = mToast.getView();
            mToast.cancel();
            mToast = new Toast(context);
            mToast.setView(view);
            mToast.setDuration(Toast.LENGTH_SHORT);
            mToast.setText(context.getString(id));
        }
        mToast.show();
    }
}
