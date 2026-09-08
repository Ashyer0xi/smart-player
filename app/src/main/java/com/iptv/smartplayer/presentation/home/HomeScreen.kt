package com.iptv.smartplayer.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.iptv.smartplayer.data.local.entity.WatchHistoryEntity
import com.iptv.smartplayer.domain.model.Movie
import com.iptv.smartplayer.presentation.components.EmptyStateView
import com.iptv.smartplayer.presentation.components.ErrorStateView
import com.iptv.smartplayer.presentation.components.FocusableCard
import com.iptv.smartplayer.presentation.components.RatingBadge
import com.iptv.smartplayer.presentation.components.SectionHeaderRow
import com.iptv.smartplayer.presentation.components.ShimmerPlaceholder
import com.iptv.smartplayer.presentation.components.verticalScrimBrush
import com.iptv.smartplayer.presentation.theme.IptvColors
import com.iptv.smartplayer.presentation.theme.IptvDimens
import com.iptv.smartplayer.presentation.theme.LocalAccentColor
import com.iptv.smartplayer.presentation.theme.LocalIptvTypography

/**
 * الشاشة الرئيسية — Hero متحرك أعلى الشاشة + صفوف محتوى أفقية بأسلوب Netflix.
 * ملاحظة: الشريط العلوي (AppTopBar) يُرسم فوق هذه الشاشة من مستوى الـScaffold في MainActivity
 * وليس هنا، كي يبقى ثابتاً عبر كل الشاشات الرئيسية.
 */
@Composable
fun HomeScreen(
    onMovieClick: (Movie) -> Unit,
    onPlayClick: (Movie) -> Unit,
    onContinueWatchingClick: (WatchHistoryEntity) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    when {
        state.errorMessage != null && state.trending == null -> {
            ErrorStateView(
                message = state.errorMessage.orEmpty(),
                modifier = Modifier.fillMaxSize(),
                onRetryClick = viewModel::retry,
            )
        }
        else -> {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(IptvColors.BackgroundPrimary),
                contentPadding = PaddingValues(bottom = 48.dp),
            ) {
                item {
                    if (state.isLoading && state.heroItems.isEmpty()) {
                        ShimmerPlaceholder(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxSize()
                                .aspectRatio(16f / 9f),
                        )
                    } else if (state.heroItems.isNotEmpty()) {
                        HeroBanner(
                            item = state.heroItems.first(),
                            onPlayClick = { /* يُمرَّر الفيلم الأول للتشغيل المباشر */ },
                        )
                    }
                }

                if (state.continueWatching.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(top = 24.dp)) {
                            SectionHeaderRow(
                                title = "أكمل المشاهدة",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = IptvDimens.tvOverscanMargin, vertical = 12.dp),
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = IptvDimens.tvOverscanMargin),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                items(state.continueWatching) { entry ->
                                    ContinueWatchingCard(entry = entry, onClick = { onContinueWatchingClick(entry) })
                                }
                            }
                        }
                    }
                }

                state.trending?.let { row ->
                    item {
                        ContentRowSection(
                            row = row,
                            onMovieClick = onMovieClick,
                            modifier = Modifier.padding(top = 24.dp),
                        )
                    }
                }

                if (state.isLoading) {
                    item {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = IptvDimens.tvOverscanMargin, vertical = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            repeat(5) {
                                ShimmerPlaceholder(
                                    modifier = Modifier.size(IptvDimens.posterCardWidthTv, IptvDimens.posterCardHeightTv),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroBanner(item: HeroItem, onPlayClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f),
    ) {
        AsyncImage(
            model = item.backdropUrl,
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        // تدرج Scrim لضمان وضوح النص فوق الخلفية
        Box(modifier = Modifier.fillMaxSize().background(verticalScrimBrush()))

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = IptvDimens.tvOverscanMargin, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = item.title,
                style = LocalIptvTypography.current.display,
                color = IptvColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (item.description.isNotBlank()) {
                Text(
                    text = item.description,
                    style = LocalIptvTypography.current.body,
                    color = IptvColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(520.dp),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = onPlayClick,
                    colors = ButtonDefaults.buttonColors(containerColor = LocalAccentColor.current),
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("تشغيل")
                }
                OutlinedButton(onClick = { /* أضف للمفضلة */ }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("أضف للمفضلة")
                }
            }
        }
    }
}

@Composable
private fun ContentRowSection(
    row: ContentRow,
    onMovieClick: (Movie) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionHeaderRow(
            title = row.title,
            onSeeAllClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = IptvDimens.tvOverscanMargin, vertical = 12.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = IptvDimens.tvOverscanMargin),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(row.movies) { movie ->
                MoviePosterCard(movie = movie, onClick = { onMovieClick(movie) })
            }
        }
    }
}

/** بطاقة البوستر القياسية 2:3 — تُستخدم في كل صفوف المحتوى بالتطبيق */
@Composable
fun MoviePosterCard(movie: Movie, onClick: () -> Unit) {
    FocusableCard(
        onClick = onClick,
        modifier = Modifier.size(IptvDimens.posterCardWidthTv, IptvDimens.posterCardHeightTv),
    ) { isFocused ->
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(IptvDimens.cardCornerRadius)),
            )
            if (isFocused) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.75f))
                        .padding(8.dp),
                ) {
                    Column {
                        Text(
                            text = movie.title,
                            style = LocalIptvTypography.current.caption,
                            color = IptvColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            movie.rating?.let { Text("⭐ %.1f".format(it), style = LocalIptvTypography.current.caption, color = IptvColors.TextSecondary) }
                            movie.releaseYear?.take(4)?.let { Text(it, style = LocalIptvTypography.current.caption, color = IptvColors.TextSecondary) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContinueWatchingCard(entry: WatchHistoryEntity, onClick: () -> Unit) {
    FocusableCard(
        onClick = onClick,
        modifier = Modifier.size(IptvDimens.episodeCardWidth, IptvDimens.posterCardHeightTv),
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = entry.posterUrl,
                contentDescription = entry.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(IptvDimens.cardCornerRadius)),
            )
            Box(modifier = Modifier.fillMaxSize().background(verticalScrimBrush()))
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)) {
                Text(
                    text = entry.title,
                    style = LocalIptvTypography.current.caption,
                    color = IptvColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                // شريط التقدم — يعكس progressFraction من الكيان مباشرة
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(IptvColors.TextDisabled),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(entry.progressFraction)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(LocalAccentColor.current),
                    )
                }
            }
        }
    }
}
