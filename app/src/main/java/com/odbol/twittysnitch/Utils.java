package com.odbol.twittysnitch;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by tyler on 10/8/17.
 */

public class Utils {

    public static void sendEmail(Context context, String subject, String body, String to) {
        sendEmail(context, subject, body, to, null, null);
    }

    public static void sendEmail(Context context, String subject, String body, String to, Uri attachment, String mimeType) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        if (mimeType == null) {
            mimeType = "*/*";
        } else {
            mimeType = Intent.normalizeMimeType(mimeType);
        }

        if (subject != null) {
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        if (to != null) {
            intent.putExtra(Intent.EXTRA_EMAIL, new String[] {to});
        }
        if (body != null) {
            intent.putExtra(Intent.EXTRA_TEXT, body);
        }
        if (attachment != null) {
            intent.setDataAndType(attachment, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            intent.putExtra(Intent.EXTRA_STREAM, attachment);
        } else {
            intent.setType(mimeType);
        }


        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.action_export)));
        }
    }

    public static String formatDate(Date timestamp) {
        return SimpleDateFormat.getDateInstance(DateFormat.FULL).format(timestamp);
    }

    public static String formatTime(Date timestamp) {
        return SimpleDateFormat.getTimeInstance(DateFormat.SHORT).format(timestamp);
    }
}
