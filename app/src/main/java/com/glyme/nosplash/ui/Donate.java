package com.glyme.nosplash.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.net.URLEncoder;

/**
 * Created by Glyme on 2016/11/20.
 */
public class Donate {
    public static boolean openWeChatPayPage(Context context) {
        throw new RuntimeException("method not implemented!");
//        return openAlipayPayPage(context, "https://qr.alipay.com/aplmqblskgweh1g5e7");
    }

    public static boolean openAlipayPayPage(Context context) {
        return openAlipayPayPage(context, "https://qr.alipay.com/aplmqblskgweh1g5e7");
    }

    public static boolean openAlipayPayPage(Context context, String qrcode) {
        try {
            qrcode = URLEncoder.encode(qrcode, "utf-8");
        } catch (Exception e) {
        }
        try {
            final String alipayqr = "alipayqr://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=" + qrcode;
            Uri uri = Uri.parse(alipayqr + "%3F_s%3Dweb-other&_t=" + System.currentTimeMillis());
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(uri);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
