package com.odbol.twittysnitch;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;


/**
 * Exports data to a file.
 */
public class FileExporter {
    private static final String TAG = "FileExporter";

    /**
     * Must match the dir in filepaths.xml
     */
    public static final String FILES_EXPORT_DIR = "export/";

    private final Context context;
    private final TweetRepo repo;

    public FileExporter(Context context, TweetRepo repo) {
        this.context = context;
        this.repo = repo;
    }

    public Single<File> saveAllEntries(final String filename) {

        return Single.create(new SingleOnSubscribe<File>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<File> emitter) throws Exception {

                File exportDir = new File(context.getExternalFilesDir(null), FILES_EXPORT_DIR);
                if (!exportDir.mkdirs() && !exportDir.isDirectory()) {
                    throw new IOException("Failed to make directory");
                }
                File file = new File(exportDir, filename);

                Log.d(TAG, "Saving to file " + file);

                try (FileWriter writer = new FileWriter(file)) {
                    List<File> files = repo.list();
                    writeEntries(writer, files);
                }

                emitter.onSuccess(file);
            }
        });

    }

    public void exportAsEmail() {
        saveAllEntries("moji-me.json")
                .subscribeOn(Schedulers.io())
                .map(file -> FileProvider.getUriForFile(context,
                        context.getString(R.string.file_provider_authority),
                        file))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(file -> {
                            Utils.sendEmail(context,
                                    context.getString(R.string.share_subject) + " " + new Date().toString(),
                                    "",
                                    null,
                                    file,
                                    null);
                            // TODO: specify spreadsheet type for CSV!
                        },
                        error -> {
                            Log.e(TAG, "Failed to save file", error);

                            Toast.makeText(context, R.string.export_error, Toast.LENGTH_LONG).show();
                        });
    }

    public static void writeEntries(Appendable writer, List<File> entries) throws IOException {

        int i = 0;
        writer.append('[');
        for (File file : entries) {
            file
            if (i++ > 0) {
                writer.append(",\n");
            }
            writer.
            shellEntry.copyFrom(entry);
            writer.append(serializer.serialize(shellEntry));
        }
        writer.append(']');
    }
}
