package com.iptv.smartplayer.presentation.series

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import com.iptv.smartplayer.domain.model.Episode
import com.iptv.smartplayer.presentation.components.FocusableCard
import com.iptv.smartplayer.presentation.components.verticalScrimBrush
import com.iptv.smartplayer.presentation.theme.IptvColors
import com.iptv.smartplayer.presentation.theme.IptvDimens
import com.iptv.smartplayer.presentation.theme.LocalAccentColor
import com.iptv.smartplayer.presentation.theme.LocalIptvTypography

/**
 * شاشة تفاصيل المسلسل: رأس Backdrop + معلومات + محدد مواسم أفقي + قائمة حلقات عريضة.
 */
@Composable
fun SeriesDetailsScreen(
    onEpisodeClick: (Episode) -> Unit,
    viewModel: SeriesDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val series = state.series

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(IptvColors.BackgroundPrimary),
        contentPadding = PaddingValues(bottom = 48.dp),
    ) {
        item {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(21f / 9f)) {
                AsyncImage(
                    model = series?.backdropUrl ?: series?.posterUrl,
                    contentDescription = series?.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(modifier = Modifier.fillMaxSize().background(verticalScrimBrush()))
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = IptvDimens.tvOverscanMargin, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = series?.title.orEmpty(),
                        style = LocalIptvTypography.current.display,
                        color = IptvColors.TextPrimary,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "${series?.numberOfSeasons ?: 0} مواسم",
                            style = LocalIptvTypography.current.body,
                            color = IptvColors.TextSecondary,
                        )
                        series?.rating?.let {
                            Text("⭐ %.1f".format(it), style = LocalIptvTypography.current.body, color = IptvColors.TextSecondary)
                        }
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = IptvDimens.tvOverscanMargin, vertical = 16.dp)) {
                series?.overview?.let {
                    Text(it, style = LocalIptvTypography.current.body, color = IptvColors.TextSecondary, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        // محدد المواسم — شريط أفقي من الشرائح (Chips)
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = IptvDimens.tvOverscanMargin),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.seasons) { season ->
                    val isSelected = season.seasonNumber == state.selectedSeasonNumber
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(IptvDimens.chipCornerRadius))
                            .background(if (isSelected) LocalAccentColor.current else IptvColors.SurfaceElevated)
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                            .then(Modifier),
                    ) {
                        Text(
                            text = "الموسم ${season.seasonNumber}",
                            style = LocalIptvTypography.current.body,
                            color = IptvColors.TextPrimary,
                            modifier = Modifier,
                        )
                    }
                }
            }
        }

        item { androidx.compose.foundation.layout.Spacer(Modifier.height(20.dp)) }

        // قائمة الحلقات — بطاقة أفقية عريضة لكل حلقة
        items(state.currentEpisodes) { episode ->
            EpisodeRow(
                episode = episode,
                onClick = { onEpisodeClick(episode) },
                modifier = Modifier.padding(horizontal = IptvDimens.tvOverscanMargin, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun EpisodeRow(episode: Episode, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FocusableCard(onClick = onClick, modifier = modifier.fillMaxWidth().height(120.dp)) { _ ->
        Row(modifier = Modifier.fillMaxSize().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.width(180.dp).fillMaxSize()) {
                AsyncImage(
                    model = episode.thumbnailUrl,
                    contentDescription = episode.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                )
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = IptvColors.TextPrimary,
                    modifier = Modifier.align(Alignment.Center).height(32.dp),
                )
                if (episode.watchProgressFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(IptvColors.TextDisabled),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(episode.watchProgressFraction)
                                .height(3.dp)
                                .background(LocalAccentColor.current),
                        )
                    }
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text(
                    text = "الحلقة ${episode.episodeNumber} · ${episode.title}",
                    style = LocalIptvTypography.current.title,
                    color = IptvColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                episode.durationMinutes?.let {
                    Text("$it دقيقة", style = LocalIptvTypography.current.caption, color = IptvColors.TextSecondary)
                }
                episode.plot?.let {
                    Text(
                        it,
                        style = LocalIptvTypography.current.caption,
                        color = IptvColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
