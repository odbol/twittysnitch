package com.odbol.twittysnitch;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
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

                try (FileOutputStream writer = new FileOutputStream(file)) {
                    List<File> files = repo.list();
                    writeEntries(writer, files);
                }

                emitter.onSuccess(file);
            }
        });

    }

    public Completable exportAsEmail() {
        return saveAllEntries("twittysnitches.txt")
                .subscribeOn(Schedulers.io())
                .map(file -> FileProvider.getUriForFile(context,
                        context.getString(R.string.file_provider_authority),
                        file))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(file -> {
                    Utils.sendEmail(context,
                            context.getString(R.string.share_subject) + " " + new Date().toString(),
                            "",
                            null,
                            file,
                            null);
                    // TODO: specify spreadsheet type for CSV!
                })
                .flatMapCompletable(file -> Completable.complete());
    }

    public static void writeEntries(FileOutputStream writer, List<File> entries) throws IOException {
        for (File file : entries) {
            byte[] buffer = new byte[1024];
            try(FileInputStream source = new FileInputStream(file)) {
                int length;
                while ((length = source.read(buffer)) > 0) {
                    writer.write(buffer, 0, length);
                }
                writer.write('\n');
            }
        }
    }
}
