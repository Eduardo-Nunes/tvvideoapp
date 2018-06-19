package com.nunes.eduardo.videoapp

import android.app.Notification
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.scheduler.Scheduler
import com.google.android.exoplayer2.ui.DownloadNotificationUtil

private const val DOWNLOAD_NOTIFICATION_ID : Int = 0
private const val DOWNLOAD_CHANNEL_ID: String = "Baixando Filme"

class VideoDownloadService : DownloadService {

    constructor(): super(DOWNLOAD_NOTIFICATION_ID)

    override fun getDownloadManager(): DownloadManager {
        return DownloadUtil.getDownloadManager(this)
    }

    override fun getForegroundNotification(taskStates: Array<out DownloadManager.TaskState>?): Notification {
        return DownloadNotificationUtil.buildProgressNotification(
                this,
                R.drawable.exo_icon_play,
                DOWNLOAD_CHANNEL_ID,
                null, "Baixando Filme", taskStates)
    }

    override fun getScheduler(): Scheduler? {
        return null
    }
}
