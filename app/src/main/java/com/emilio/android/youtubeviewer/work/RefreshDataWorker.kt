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

package com.emilio.android.youtubeviewer.work

import android.app.Application
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.emilio.android.youtubeviewer.YoutubePlayerApplication
import com.emilio.android.youtubeviewer.database.VideosDatabase
import com.emilio.android.youtubeviewer.database.getDatabase
import com.emilio.android.youtubeviewer.repository.VideosRepository
import retrofit2.HttpException
import timber.log.Timber

class RefreshDataWorker(appContext: Context, params: WorkerParameters) :
        CoroutineWorker(appContext, params) {

    companion object {
        const val WORK_NAME = "com.emilio.android.youtubeviewer.work.RefreshDataWorker"
    }
    override suspend fun doWork(): Result {

        val database = getDatabase(this.applicationContext)
        val repository = VideosRepository(database)

        try {
            repository.refreshVideos( )
            Timber.d("### WorkManager ###: Work request for sync has executed!")
        } catch (e: HttpException) {
            return Result.retry()
        }
        return Result.success()
    }
}