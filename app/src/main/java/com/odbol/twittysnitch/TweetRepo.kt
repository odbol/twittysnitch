package com.odbol.twittysnitch

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.io.StringWriter
import java.util.concurrent.TimeUnit

class TweetRepo(private val context: Context) {

    private val dbDir = context.getDir("tweets", MODE_PRIVATE)
    private val tweetCache = mutableSetOf<String>()

    private var lastFlushMs: Long = 0

    fun save(tweet: CharSequence) {
        tweetCache.add(tweet.toString())

        if (tweetCache.size > MAX_SIZE || System.currentTimeMillis() > lastFlushMs + MAX_TIME_MS) {
            flushCache()
        }
    }

    private fun flushCache() {
        val currentTimeMillis = System.currentTimeMillis()
        if (tweetCache.size > 0) {
            Single.just(tweetCache.toSet())
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .map { tweets ->
                    val dbFile = File(dbDir, "tweets-$currentTimeMillis")
                    dbFile.outputStream().bufferedWriter().use { writer ->
                        tweets.forEach {
                            writer.write(it)
                            writer.newLine()
                        }
                    }
                    tweets
                }
                .subscribe(
                    // onSuccess
                    {
                        Toast.makeText(context, "Saved ${it.size} tweets to db", Toast.LENGTH_SHORT)
                            .show()
                    },
                    // onError
                    {
                        Log.e(TAG, "Failed to write db file", it)
                        Toast.makeText(
                            context,
                            "Failed to save Tweets db ${it.message}",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                )
        }
        tweetCache.clear()
        lastFlushMs = currentTimeMillis
    }

    companion object {
        private const val TAG = "TweetRepo"
        private const val MAX_SIZE = 300
        private val MAX_TIME_MS = TimeUnit.MINUTES.toMillis(20)
    }
}
