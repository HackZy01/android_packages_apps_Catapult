/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.view

import android.animation.AnimatorInflater
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.tvprovider.media.tv.BasePreviewProgram
import coil.load
import com.google.android.material.progressindicator.LinearProgressIndicator
import org.lineageos.tv.launcher.R
import org.lineageos.tv.launcher.ext.displayTitle
import org.lineageos.tv.launcher.ext.getAttributeResourceId
import kotlin.math.abs
import kotlin.math.roundToInt

class WatchNextCard @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : Card(context, attrs, defStyleAttr) {
    // Views
    private val cardRoot: View by lazy { findViewById(R.id.watch_next_card_root)!! }
    private val bannerView: ImageView by lazy { findViewById(R.id.app_banner)!! }
    private val title: TextView by lazy { findViewById(R.id.title)!! }
    private val progressView: LinearProgressIndicator by lazy { findViewById(R.id.watch_progress)!! }

    private var appliedAspectRatio = DEFAULT_ASPECT_RATIO

    init {
        inflate(context, R.layout.watch_next_card, this)

        setupFocusShadow(resources.getDimension(R.dimen.card_radius))

        stateListAnimator =
            AnimatorInflater.loadStateListAnimator(context, R.animator.app_card_state_animator)

        setupNameMarquee()
    }

    private fun setupNameMarquee() {
        setOnFocusChangeListener { _, hasFocus ->
            title.isInvisible = !hasFocus
            if (hasFocus) {
                title.postDelayed({ title.isSelected = true }, 2000)
            } else {
                title.isSelected = false
            }
        }
    }

    /**
     * Keep the card height fixed and derive its width from the artwork's aspect ratio, so that
     * portrait, square and landscape artwork all fill the card without bars or heavy cropping.
     */
    private fun applyAspectRatio(aspectRatio: Float) {
        val ratio = aspectRatio.coerceIn(MIN_ASPECT_RATIO, MAX_ASPECT_RATIO)
        val targetWidth = (resources.getDimensionPixelSize(R.dimen.watch_next_card_height) * ratio)
            .roundToInt()

        appliedAspectRatio = ratio
        cardRoot.updateLayoutParams { width = targetWidth }
        title.updateLayoutParams { width = targetWidth }
    }

    @Suppress("RestrictedApi")
    fun setInfo(info: BasePreviewProgram) {
        // Use the ratio declared by the app for a stable first layout, the real image refines it
        applyAspectRatio(declaredAspectRatio(info.posterArtAspectRatio) ?: DEFAULT_ASPECT_RATIO)

        title.isInvisible = true
        label = info.title ?: ""
        bannerView.isVisible = true
        launchIntent = info.intent
        title.text = info.displayTitle(context)
        progressView.isInvisible = true

        if (info.lastPlaybackPositionMillis != -1 && info.durationMillis != -1) {
            val percentWatched =
                ((info.lastPlaybackPositionMillis.toDouble() / info.durationMillis) * 100).toInt()
            if (percentWatched > 3) {
                progressView.progress = percentWatched
                progressView.isVisible = true
            }
        }

        bannerView.load(info.posterArtUri) {
            placeholder(
                context.getAttributeResourceId(
                    com.google.android.material.R.attr.colorSecondaryContainer
                )
            )
            crossfade(500)
            listener(onSuccess = { _, result ->
                val drawable = result.drawable
                if (drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0) {
                    val actualRatio = drawable.intrinsicWidth.toFloat() / drawable.intrinsicHeight
                    val relativeDiff = abs(actualRatio - appliedAspectRatio) / appliedAspectRatio
                    if (relativeDiff > ASPECT_RATIO_TOLERANCE) {
                        applyAspectRatio(actualRatio)
                    }
                }
            })
        }
    }

    companion object {
        private const val DEFAULT_ASPECT_RATIO = 16f / 9f
        private const val MIN_ASPECT_RATIO = 0.5f
        private const val MAX_ASPECT_RATIO = 2.4f
        private const val ASPECT_RATIO_TOLERANCE = 0.05f

        // Values of the TV provider contract's ASPECT_RATIO_* constants
        private fun declaredAspectRatio(value: Int): Float? = when (value) {
            0 -> 16f / 9f // ASPECT_RATIO_16_9
            1 -> 3f / 2f // ASPECT_RATIO_3_2
            2 -> 4f / 3f // ASPECT_RATIO_4_3
            3 -> 1f // ASPECT_RATIO_1_1
            4, 5 -> 2f / 3f // ASPECT_RATIO_2_3, ASPECT_RATIO_MOVIE_POSTER
            else -> null
        }
    }
}
