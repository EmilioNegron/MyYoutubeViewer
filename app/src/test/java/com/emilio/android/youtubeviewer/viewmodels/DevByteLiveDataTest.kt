package com.emilio.android.youtubeviewer.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.emilio.android.youtubeviewer.database.getDatabase
import com.emilio.android.youtubeviewer.repository.VideosRepository
import getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class DevByteLiveDataTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var rule = MockitoJUnit.rule()

    private val testDispatcher = TestCoroutineDispatcher()

    // Subject under test
    private lateinit var viewModel: DevByteViewModel

    @Mock
    private lateinit var repo: VideosRepository

    @Before
    fun setupViewModel() {
        // Sets the given [dispatcher] as an underlying dispatcher of [Dispatchers.Main].
        // All consecutive usages of [Dispatchers.Main] will use given [dispatcher] under the hood.
        Dispatchers.setMain(testDispatcher)

        viewModel = DevByteViewModel(ApplicationProvider.getApplicationContext())

        repo = VideosRepository(getDatabase(ApplicationProvider.getApplicationContext()))
    }

    @After
    fun tearDown() {
        // Resets state of the [Dispatchers.Main] to the original main dispatcher.
        // For example, in Android Main thread dispatcher will be set as [Dispatchers.Main].
        Dispatchers.resetMain()

        // Clean up the TestCoroutineDispatcher to make sure no other work is running.
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `call reFreshVideos and verify that eventNetworkError is set to false`() = runBlocking {
        suspend { repo.refreshVideos() }

        junit.framework.Assert.assertNotNull(viewModel.eventNetworkError)
        viewModel.eventNetworkError.let { junit.framework.Assert.assertFalse(it.getOrAwaitValue()) }

        return@runBlocking
    }

    @Test
    fun `call reFreshVideos and verify that isNetworkErrorShown is set to false`() = runBlocking {
        suspend { repo.refreshVideos() }

        junit.framework.Assert.assertNotNull(viewModel.isNetworkErrorShown)
        viewModel.isNetworkErrorShown.let { junit.framework.Assert.assertFalse(it.getOrAwaitValue()) }

        return@runBlocking
    }

}