package com.jetstream.android.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import com.jetstream.android.proto.Fullscreen
import com.jetstream.android.proto.MessageWrapper
import com.jetstream.android.proto.NextSlide
import com.jetstream.android.proto.Presentation
import com.jetstream.android.proto.PrevSlide
import com.jetstream.android.proto.Visibility
import com.jetstream.android.services.jetstream.JetStreamRepository

class PresentationRemoteScreenViewModel: ViewModel() {
    private val TAG = "PresentationViewModel"

    fun sendPresentationPrevious() {
        val wrapper = MessageWrapper(
            presentation = Presentation(
                prevslide = PrevSlide()
            )
        )
        JetStreamRepository.wsSend(MessageWrapper.ADAPTER.encode(wrapper))
        Log.d(TAG, "Prev slide message sent")
    }

    fun sendPresentationNext() {
        val wrapper = MessageWrapper(
            presentation = Presentation(
                nextslide = NextSlide()
            )
        )
        JetStreamRepository.wsSend(MessageWrapper.ADAPTER.encode(wrapper))
        Log.d(TAG, "Next slide message sent")
    }

    fun sendPresentationFullscreen() {
        val wrapper = MessageWrapper(
            presentation = Presentation(
                fullscreen = Fullscreen()
            )
        )
        JetStreamRepository.wsSend(MessageWrapper.ADAPTER.encode(wrapper))
        Log.d(TAG, "Fullscreen slide message sent")
    }

    fun sendPresentationVisibility() {
        val wrapper = MessageWrapper(
            presentation = Presentation(
                visibility = Visibility()
            )
        )
        JetStreamRepository.wsSend(MessageWrapper.ADAPTER.encode(wrapper))
        Log.d(TAG, "Slide visibility message sent")
    }
}