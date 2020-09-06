package com.emilio.android.youtubeviewer.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.emilio.android.youtubeviewer.R
import com.emilio.android.youtubeviewer.YoutubePlayerApplication
import com.emilio.android.youtubeviewer.databinding.FragmentHelloBinding
import com.emilio.android.youtubeviewer.viewmodels.DevByteViewModel
import com.jakewharton.processphoenix.ProcessPhoenix
import timber.log.Timber


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HelloFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HelloFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val binding: FragmentHelloBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_hello,
                container,
                false)

        // Set the lifecycleOwner so DataBinding can observe LiveData
        binding.lifecycleOwner = viewLifecycleOwner

        binding.goToDevByteScreen = GoToDevByteScreen {
            try {
                findNavController().navigate(HelloFragmentDirections.actionHelloFragmentToDevByte())
            } catch (e: IllegalStateException) {
                Timber.d("""Error: ${e.message}""")
                Timber.d("Current bug in Android Fragment Navigation API. Need to find work-around.")
                ProcessPhoenix.triggerRebirth(context);
            }
        }

        return binding.root
    }
}

/**
 * Click listener for Navigation from HelloFragment to the DevByte Fragment.
 *
 */
class GoToDevByteScreen(val block: () -> Unit) {
    fun onClick() = block()
}

