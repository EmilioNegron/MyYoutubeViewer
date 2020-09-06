/*
 * Copyright (C) 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.emilio.android.youtubeviewer

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.*

import com.emilio.android.youtubeviewer.work.RefreshDataWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception
import java.util.concurrent.TimeUnit

/**
 * Override application to setup background work via WorkManager
 */
open class YoutubePlayerApplication : Application() {

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    /**
     * onCreate is called before the first screen is shown to the user.
     *
     * Use it to setup any background tasks, running expensive setup operations in a background
     * thread to avoid delaying app start.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate() {
        super.onCreate()
        delayedInit()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun delayedInit() {
        applicationScope.launch {
            Timber.plant(Timber.DebugTree())
            setupRecurringWork()
        }
    }

    /**
     * Setup WorkManager background job to 'fetch' new network data daily.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun setupRecurringWork() {

        val constraints = Constraints.Builder()
                .setRequiresCharging(true)
                .setRequiresBatteryNotLow(true)
                .apply {
                    setRequiredNetworkType(NetworkType.CONNECTED)
                }
                .build()

        val repeatingRequest = PeriodicWorkRequest
                .Builder(RefreshDataWorker::class.java, 1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()

        Timber.d("### WorkManager ###: Periodic Work request for sync is scheduled")
        try {
            WorkManager.getInstance().enqueueUniquePeriodicWork(
                    RefreshDataWorker.WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    repeatingRequest)
        } catch (e: Exception ) {
            Timber.d("Error ==> ${e.message}")
        }
    }
}
