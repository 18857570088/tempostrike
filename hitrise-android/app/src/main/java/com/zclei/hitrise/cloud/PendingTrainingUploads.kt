package com.zclei.hitrise.cloud

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

/** App-private, durable snapshots. A server-side client_session_id makes retries idempotent. */
object PendingTrainingUploads {
    private const val PREFS_NAME = "tempostrike_pending_uploads"
    private const val KEY_ITEMS = "items_v1"
    private const val JOB_ID = 0x545301
    private val drainLock = Any()
    private val storageLock = Any()

    data class Item(val key: String, val payload: JSONObject)

    fun enqueue(context: Context, key: String, payload: JSONObject): Boolean = synchronized(storageLock) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val items = readItems(prefs.getString(KEY_ITEMS, null))
        val updated = JSONArray()
        for (index in 0 until items.length()) {
            val item = items.optJSONObject(index) ?: continue
            if (item.optString("key") != key) updated.put(item)
        }
        updated.put(JSONObject().put("key", key).put("payload", payload))
        prefs.edit().putString(KEY_ITEMS, updated.toString()).commit()
    }

    fun hasPending(context: Context): Boolean = synchronized(storageLock) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        readItems(prefs.getString(KEY_ITEMS, null)).length() > 0
    }

    fun schedule(context: Context) {
        if (!hasPending(context)) return
        val appContext = context.applicationContext
        val scheduler = appContext.getSystemService(JobScheduler::class.java) ?: return
        val job = JobInfo.Builder(JOB_ID, ComponentName(appContext, PendingTrainingUploadJobService::class.java))
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPersisted(true)
            .setBackoffCriteria(30_000L, JobInfo.BACKOFF_POLICY_EXPONENTIAL)
            .build()
        scheduler.schedule(job)
    }

    /** Returns true when the queue is empty. Stops at the first failed upload to preserve order. */
    fun drain(context: Context, service: CloudSyncService, onUploaded: ((CloudSessionUploadResult) -> Unit)? = null): Boolean = synchronized(drainLock) {
            while (true) {
                val item = peek(context) ?: return@synchronized true
                val result = service.uploadTrainingPayload(item.payload)
                if (!result.success) return@synchronized false
                removeIfUnchanged(context, item)
                onUploaded?.invoke(result)
            }
            @Suppress("UNREACHABLE_CODE")
            false
    }

    private fun peek(context: Context): Item? = synchronized(storageLock) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val item = readItems(prefs.getString(KEY_ITEMS, null)).optJSONObject(0) ?: return@synchronized null
        val payload = item.optJSONObject("payload") ?: return@synchronized null
        Item(item.optString("key"), payload)
    }

    private fun removeIfUnchanged(context: Context, uploaded: Item) = synchronized(storageLock) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val items = readItems(prefs.getString(KEY_ITEMS, null))
        val remaining = JSONArray()
        for (index in 0 until items.length()) {
            val item = items.optJSONObject(index) ?: continue
            val sameSnapshot = item.optString("key") == uploaded.key &&
                item.optJSONObject("payload")?.toString() == uploaded.payload.toString()
            if (!sameSnapshot) remaining.put(item)
        }
        prefs.edit().putString(KEY_ITEMS, remaining.toString()).commit()
    }

    private fun readItems(raw: String?): JSONArray =
        try { JSONArray(raw ?: "[]") } catch (_: Exception) { JSONArray() }
}

class PendingTrainingUploadJobService : JobService() {
    @Volatile private var stopped = false

    override fun onStartJob(params: JobParameters): Boolean {
        stopped = false
        Thread {
            val empty = try {
                PendingTrainingUploads.drain(applicationContext, CloudSyncService())
            } catch (error: Exception) {
                Log.w("TrainingUpload", "Background upload failed", error)
                false
            }
            if (!stopped) jobFinished(params, !empty)
        }.start()
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        stopped = true
        return PendingTrainingUploads.hasPending(applicationContext)
    }
}
