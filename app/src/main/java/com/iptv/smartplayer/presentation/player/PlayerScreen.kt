package com.iptv.smartplayer.presentation.player

import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import com.iptv.smartplayer.presentation.components.ErrorStateView
import com.iptv.smartplayer.presentation.theme.IptvColors
import com.iptv.smartplayer.presentation.theme.IptvDimens
import com.iptv.smartplayer.presentation.theme.LocalAccentColor
import com.iptv.smartplayer.presentation.theme.LocalIptvTypography

/**
 * شاشة المشغل — طبقة فيديو (ExoPlayer PlayerView) + Overlay شفاف يظهر/يختفي تلقائياً.
 * أزرار الترجيع/التقديم تظهر فقط لـVOD؛ القناة السابقة/التالية تظهر فقط للبث المباشر،
 * حسب مواصفات "واجهة المشغل" بالضبط.
 */
@Composable
fun PlayerScreen(
    onBackClick: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black)
            .clickable(
                indication = null,
                interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            ) { viewModel.onUserInteraction() },
    ) {
        // طبقة الفيديو: PlayerView لـExoPlayer أو SurfaceView لـlibmpv، حسب المحرك النشط حالياً.
        // كلاهما يبقى في الشجرة بنفس الـModifier؛ Compose يتولى إعادة التركيب بينهما بسلاسة
        // لأن كل واحد منهما AndroidView منفصل بمفتاح ثابت (activeEngine كجزء من الحالة).
        when (state.activeEngine) {
            ActiveEngine.EXOPLAYER -> {
                AndroidView(
                    factory = {
                        PlayerView(context).apply {
                            player = viewModel.exoPlayer
                            useController = false
                            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            ActiveEngine.LIBMPV -> {
                MpvPlayerView(mpvController = viewModel.mpvController, modifier = Modifier.fillMaxSize())
            }
        }

        if (state.isBuffering && state.errorMessage == null) {
            CircularProgressIndicator(
                color = LocalAccentColor.current,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        // رسالة عابرة تُعلم المستخدم بصمت أن التطبيق بدّل المحرك تلقائياً لإنقاذ التشغيل
        AnimatedVisibility(
            visible = state.didFallbackToMpv,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 80.dp),
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(IptvColors.SurfaceElevated)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text("تم التبديل لمحرك تشغيل بديل لضمان استمرار العرض", style = LocalIptvTypography.current.caption, color = IptvColors.TextPrimary)
            }
        }

        state.errorMessage?.let { message ->
            ErrorStateView(message = message, modifier = Modifier.fillMaxSize(), onRetryClick = viewModel::retry)
        }

        AnimatedVisibility(
            visible = state.isControlsVisible && state.errorMessage == null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize(),
        ) {
            PlayerOverlay(state = state, viewModel = viewModel, onBackClick = onBackClick)
        }
    }
}

@Composable
private fun PlayerOverlay(state: PlayerUiState, viewModel: PlayerViewModel, onBackClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        // الأعلى: اسم المحتوى + رجوع
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(IptvColors.ScrimEnd, IptvColors.ScrimStart)))
                .padding(horizontal = IptvDimens.tvOverscanMargin, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع", tint = IptvColors.TextPrimary)
            }
            if (state.contentType == PlaybackContentType.LIVE) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(IptvColors.StateLiveBadge)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text("مباشر", style = LocalIptvTypography.current.caption, color = IptvColors.TextPrimary)
                }
            }
            Text(state.title, style = LocalIptvTypography.current.title, color = IptvColors.TextPrimary)
            if (state.activeEngine == ActiveEngine.LIBMPV) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(IptvColors.SurfaceElevated)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text("mpv", style = LocalIptvTypography.current.caption, color = IptvColors.TextSecondary)
                }
            }
        }

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))

        // الوسط: أزرار التحكم — تختلف حسب النوع (VOD أو مباشر)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (state.contentType == PlaybackContentType.VOD) {
                IconButton(onClick = { viewModel.seekBy(-10_000) }) {
                    Icon(Icons.Filled.FastRewind, contentDescription = "ترجيع", tint = IptvColors.TextPrimary, modifier = Modifier.size(36.dp))
                }
            } else {
                IconButton(onClick = { /* القناة السابقة */ }) {
                    Icon(Icons.Filled.SkipPrevious, contentDescription = "القناة السابقة", tint = IptvColors.TextPrimary, modifier = Modifier.size(36.dp))
                }
            }

            IconButton(
                onClick = viewModel::togglePlayPause,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(LocalAccentColor.current),
            ) {
                Icon(
                    imageVector = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = IptvColors.TextPrimary,
                    modifier = Modifier.size(36.dp),
                )
            }

            if (state.contentType == PlaybackContentType.VOD) {
                IconButton(onClick = { viewModel.seekBy(10_000) }) {
                    Icon(Icons.Filled.FastForward, contentDescription = "تقديم", tint = IptvColors.TextPrimary, modifier = Modifier.size(36.dp))
                }
            } else {
                IconButton(onClick = { /* القناة التالية */ }) {
                    Icon(Icons.Filled.SkipNext, contentDescription = "القناة التالية", tint = IptvColors.TextPrimary, modifier = Modifier.size(36.dp))
                }
            }
        }

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))

        // الأسفل: شريط التقدم (VOD) أو ملخص EPG (مباشر) + أزرار إعدادات سريعة
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(IptvColors.ScrimStart, IptvColors.ScrimEnd)))
                .padding(horizontal = IptvDimens.tvOverscanMargin, vertical = 16.dp),
        ) {
            if (state.contentType == PlaybackContentType.VOD && state.durationMillis > 0) {
                val progress = (state.positionMillis.toFloat() / state.durationMillis).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(IptvColors.TextDisabled),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(LocalAccentColor.current),
                    )
                }
                androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(formatMillis(state.positionMillis), style = LocalIptvTypography.current.caption, color = IptvColors.TextSecondary)
                    Text(formatMillis(state.durationMillis), style = LocalIptvTypography.current.caption, color = IptvColors.TextSecondary)
                }
            }

            androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.align(Alignment.End)) {
                Icon(Icons.Filled.HighQuality, contentDescription = "الجودة", tint = IptvColors.TextPrimary, modifier = Modifier.size(24.dp))
                Icon(Icons.Filled.ClosedCaption, contentDescription = "الترجمة", tint = IptvColors.TextPrimary, modifier = Modifier.size(24.dp))
                Icon(Icons.Filled.PictureInPictureAlt, contentDescription = "صورة داخل صورة", tint = IptvColors.TextPrimary, modifier = Modifier.size(24.dp))
            }
        }
    }
}

private fun formatMillis(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds) else "%d:%02d".format(minutes, seconds)
}
