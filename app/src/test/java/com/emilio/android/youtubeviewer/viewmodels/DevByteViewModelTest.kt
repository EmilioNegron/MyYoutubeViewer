package com.emilio.android.youtubeviewer.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.emilio.android.youtubeviewer.YoutubePlayerApplication
import com.emilio.android.youtubeviewer.database.VideosDatabase
import com.emilio.android.youtubeviewer.domain.DevByteVideo
import com.emilio.android.youtubeviewer.repository.VideosRepository
import junit.framework.Assert.*
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
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.Spy
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import java.io.IOException


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class DevByteViewModelTest {
    private val testDispatcher = TestCoroutineDispatcher()

    private lateinit var eventNetworkError: LiveData<Boolean>

    private val videos = listOf(
            DevByteVideo("2022 Corvette C8 Z06 Spy Shots and Exhaust SOUND! *Mid Engine Corvette*\n",
                    "The 2022 Corvette and the much anticipated brother the Z06! Get to see spy shot of the 2022 Z06, and listen to the throaty exhaust note as it zips by our cameras!",
                    "https://www.youtube.com/watch?v=jmKNnL9iI2A",
                    "2020-08-19T17:09:43+00:00",
                    "https://i2.ytimg.com/vi/jmKNnL9iI2A/hqdefault.jpg"),
            DevByteVideo("Here’s Why the 2020 Chevy Corvette C8 Is The Hottest Car of the Year\n",
                    "The 2020 Chevy Corvette C8 is the hottest car of the year -- by far. Today I'm going to review the new C8 Corvette, and I'm going to take you on a tour of the new 2020 Corvette. Then I'm going to drive the 2020 C8 Corvette and tell you what it's like on the road.",
                    "https://www.youtube.com/watch?v=yZT3hyhao-o\"",
                    "2020-03-17T16:57:23+00:00",
                    "https://i2.ytimg.com/vi/yZT3hyhao-o/hqdefault.jpg")
    )

    @Spy
    private val videoListLiveData: MutableLiveData<List<DevByteVideo>> = MutableLiveData()

    @Mock
    private lateinit var viewModel: DevByteViewModel

    @Mock
    private lateinit var repo: VideosRepository

    @Mock
    private lateinit var application: YoutubePlayerApplication

    @Mock
    private lateinit var database: VideosDatabase

    @Rule
    @JvmField
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
        // Sets the given [dispatcher] as an underlying dispatcher of [Dispatchers.Main].
        // All consecutive usages of [Dispatchers.Main] will use given [dispatcher] under the hood.
        Dispatchers.setMain(testDispatcher)

       // `when`(repo.videos).thenReturn(videoListLiveData)
        viewModel = DevByteViewModel(application)

        eventNetworkError = viewModel.eventNetworkError

    }

    @ExperimentalCoroutinesApi
    @After
    fun tearDown() {
        // Resets state of the [Dispatchers.Main] to the original main dispatcher.
        // For example, in Android Main thread dispatcher will be set as [Dispatchers.Main].
        Dispatchers.resetMain()

        // Clean up the TestCoroutineDispatcher to make sure no other work is running.
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `isNetworkError is true when repo throws exception`() = runBlocking {
        eventNetworkError = viewModel.eventNetworkError
        var isNetworkError = eventNetworkError.value
        assertNotNull(isNetworkError)
        isNetworkError?.let { assertFalse(it) }

        // Mock IOException
        suspend { `when`(viewModel.refreshDataFromRepository()).thenAnswer { throw IOException() } }

       isNetworkError = viewModel.isPlaylistEmpty()
        assertNotNull(isNetworkError)
        isNetworkError?.let { assertTrue(it) }

        suspend {
            verify(viewModel).refreshDataFromRepository()

            isNetworkError = viewModel.isPlaylistEmpty()
            assertNotNull(isNetworkError)
            isNetworkError?.let { assertFalse(it) }
        }

        return@runBlocking
    }

    @Test
    fun `retrieves local videos without calling refreshVideos`() = runBlocking {
        videoListLiveData.value = videos

        verify(repo, never()).refreshVideos()

        suspend {
            viewModel.myVideos

            val isError = viewModel.isPlaylistEmpty()
            assertNotNull(isError)
            isError?.let { assertFalse(it) }
        }

        return@runBlocking
    }

    @Test
    fun `call refreshVideos to get videos if not locally present`() = runBlocking {
        // Refresh List as usual
        var isEmpty = false
        suspend {
            verify(repo.refreshVideos())

            // verify Local List exists
            isEmpty = viewModel.isPlaylistEmpty()!!
            assertNotNull(isEmpty)
            isEmpty?.let { assertFalse(it) }
        }

        // Remove all Local videos from DB
        suspend {
            database.videoDao.deleteAllFromTable()

            isEmpty = viewModel.isPlaylistEmpty()!!
            assertNotNull(isEmpty)
            isEmpty?.let { assertTrue(it) }
        }

        // Refresh again
        suspend {
            verify((repo).refreshVideos())

            isEmpty = viewModel.isPlaylistEmpty()!!
            assertNotNull(isEmpty)
            isEmpty?.let { assertFalse(it) }
        }

        return@runBlocking
    }
}