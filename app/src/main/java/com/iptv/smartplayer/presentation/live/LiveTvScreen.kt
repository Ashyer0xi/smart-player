package com.iptv.smartplayer.presentation.live

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.iptv.smartplayer.domain.model.Channel
import com.iptv.smartplayer.presentation.components.FocusableCard
import com.iptv.smartplayer.presentation.theme.IptvColors
import com.iptv.smartplayer.presentation.theme.IptvDimens
import com.iptv.smartplayer.presentation.theme.LocalAccentColor
import com.iptv.smartplayer.presentation.theme.LocalIptvTypography

/**
 * شاشة البث المباشر: عمود تصنيفات + شبكة قنوات + شريط EPG مصغّر أسفل القناة المختارة.
 * دليل البرامج الكامل (Timeline Grid) يُبنى في PlayerScreen عند التشغيل الفعلي لتفادي
 * ازدحام هذه الشاشة، ويظهر هنا EPG القناة المختارة فقط (البرنامج الحالي + القادم).
 */
@Composable
fun LiveTvScreen(
    onChannelPlay: (Channel) -> Unit,
    viewModel: LiveTvViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Row(modifier = Modifier.fillMaxSize().background(IptvColors.BackgroundPrimary)) {
        // عمود التصنيفات (يمين الشاشة بسبب RTL)
        Column(
            modifier = Modifier
                .width(200.dp)
                .fillMaxHeight()
                .background(IptvColors.SurfaceDefault)
                .padding(vertical = 24.dp),
        ) {
            CategoryItem(name = "الكل", selected = state.selectedCategoryId == null) {
                viewModel.onCategorySelected(null)
            }
            state.categories.forEach { category ->
                CategoryItem(name = category.name, selected = state.selectedCategoryId == category.id) {
                    viewModel.onCategorySelected(category.id)
                }
            }
        }

        // شبكة/قائمة القنوات + معلومات EPG للقناة المختارة
        Column(modifier = Modifier.weight(1f).padding(IptvDimens.tvOverscanMargin)) {
            state.selectedChannel?.let { channel ->
                SelectedChannelHeader(channel = channel, epg = state.epgForSelected, onPlayClick = { onChannelPlay(channel) })
                androidx.compose.foundation.layout.Spacer(Modifier.height(20.dp))
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.channels) { channel ->
                    ChannelRow(
                        channel = channel,
                        isSelected = channel.streamId == state.selectedChannel?.streamId,
                        onClick = { viewModel.onChannelSelected(channel) },
                        onDoubleClickPlay = { onChannelPlay(channel) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryItem(name: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) IptvColors.SurfaceElevated else IptvColors.SurfaceDefault)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .then(Modifier)
            .let { it }
            .then(androidx.compose.foundation.clickable(onClick = onClick)),
    ) {
        Text(
            text = name,
            style = LocalIptvTypography.current.body,
            color = if (selected) LocalAccentColor.current else IptvColors.TextSecondary,
        )
    }
}

@Composable
private fun SelectedChannelHeader(
    channel: Channel,
    epg: List<com.iptv.smartplayer.domain.model.EpgProgram>,
    onPlayClick: () -> Unit,
) {
    val now = System.currentTimeMillis()
    val current = epg.firstOrNull { it.isCurrentlyAiring(now) }
    val next = epg.firstOrNull { it.startEpochMillis > now }

    FocusableCard(onClick = onPlayClick, modifier = Modifier.fillMaxWidth().height(140.dp)) { _ ->
        Row(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            AsyncImage(
                model = channel.logoUrl,
                contentDescription = channel.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(IptvDimens.channelLogoSize),
            )
            Column(verticalArrangement = Arrangement.Center) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(IptvColors.StateLiveBadge),
                    )
                    Text("مباشر", style = LocalIptvTypography.current.caption, color = IptvColors.StateLiveBadge)
                }
                Text(channel.name, style = LocalIptvTypography.current.title, color = IptvColors.TextPrimary)
                current?.let {
                    Text("الآن: ${it.title}", style = LocalIptvTypography.current.body, color = IptvColors.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                next?.let {
                    Text("التالي: ${it.title}", style = LocalIptvTypography.current.caption, color = IptvColors.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun ChannelRow(
    channel: Channel,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDoubleClickPlay: () -> Unit,
) {
    FocusableCard(onClick = onClick, onLongClick = onDoubleClickPlay, modifier = Modifier.fillMaxWidth().height(72.dp)) { _ ->
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = channel.logoUrl,
                contentDescription = channel.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(40.dp),
            )
            Text(
                text = channel.name,
                style = LocalIptvTypography.current.body,
                color = if (isSelected) LocalAccentColor.current else IptvColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
