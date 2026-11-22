package com.example.applocker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

/**
 * Har 15 daqiqada Telegram botdan kelgan xabarlarni tekshiradi.
 * Ruxsat etilgan chat ID'dan maxsus buyruq (masalan "/ochir") kelsa,
 * bildirishnoma chiqaradi - ustiga bosilsa sozlash paneli ochiladi.
 */
class OpenCheckWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        val c = applicationContext
        val token = Store.botToken(c)
        val allowedIds = Store.chatId(c).split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val command = Store.remoteCommand(c).lowercase()
        if (token.isBlank() || allowedIds.isEmpty()) return Result.success()

        try {
            val offset = Store.lastUpdateId(c) + 1
            val url = URL("https://api.telegram.org/bot$token/getUpdates?offset=$offset&timeout=0")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 10000; readTimeout = 10000
            }
            val text = conn.inputStream.bufferedReader().readText()
            conn.disconnect()

            val results = org.json.JSONObject(text).getJSONArray("result")
            var maxId = offset - 1
            for (i in 0 until results.length()) {
                val upd = results.getJSONObject(i)
                val updId = upd.getLong("update_id")
                if (updId > maxId) maxId = updId
                val msg = upd.optJSONObject("message") ?: continue
                val chatId = msg.getJSONObject("chat").getLong("id").toString()
                val msgText = msg.optString("text", "").trim().lowercase()
                if (chatId in allowedIds && msgText == command) {
                    notifyOpen()
                }
            }
            Store.setLastUpdateId(c, maxId)
        } catch (_: Exception) {
            // Internet yo'q yoki Telegram javob bermadi - keyingi safar qayta urinamiz
        }
        return Result.success()
    }

    private fun notifyOpen() {
        val c = applicationContext
        val channelId = "al_open"
        val mgr = c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mgr.createNotificationChannel(
                NotificationChannel(channelId, "Tizim xabarlari", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        val pi = PendingIntent.getActivity(
            c, 0, Intent(c, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(c, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Tizim yangilanishi")
            .setContentText("Bosib ko'rish uchun shu yerga teging")
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        mgr.notify(1001, n)
    }
}
