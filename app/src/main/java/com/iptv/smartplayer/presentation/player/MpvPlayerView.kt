package com.iptv.smartplayer.presentation.player

import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.iptv.smartplayer.core.player.MpvController

/**
 * يستضيف عرض فيديو libmpv داخل Compose عبر SurfaceView تقليدي (mpv يرسم مباشرة على الـSurface
 * عبر gpu-context=android بدل استخدام Compose Canvas). يُستخدم فقط عندما يكون محرك التشغيل
 * النشط هو LIBMPV — انظر PlayerScreen لمنطق الاختيار بين هذا وبين PlayerView الخاص بـExoPlayer.
 */
@Composable
fun MpvPlayerView(mpvController: MpvController, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = {
            SurfaceView(context).apply {
                holder.addCallback(
                    object : SurfaceHolder.Callback {
                        override fun surfaceCreated(holder: SurfaceHolder) {
                            mpvController.initialize(context)
                            mpvController.attachSurface(holder.surface)
                        }

                        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) = Unit

                        override fun surfaceDestroyed(holder: SurfaceHolder) {
                            mpvController.detachSurface()
                        }
                    },
                )
            }
        },
    )

    DisposableEffect(Unit) {
        onDispose { mpvController.release() }
    }
}
