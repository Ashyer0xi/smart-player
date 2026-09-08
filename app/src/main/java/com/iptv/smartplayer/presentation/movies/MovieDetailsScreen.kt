package com.iptv.smartplayer.presentation.movies

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.iptv.smartplayer.domain.model.CastMember
import com.iptv.smartplayer.domain.model.Movie
import com.iptv.smartplayer.presentation.components.ErrorStateView
import com.iptv.smartplayer.presentation.components.RatingBadge
import com.iptv.smartplayer.presentation.components.SectionHeaderRow
import com.iptv.smartplayer.presentation.components.ShimmerPlaceholder
import com.iptv.smartplayer.presentation.components.verticalScrimBrush
import com.iptv.smartplayer.presentation.home.MoviePosterCard
import com.iptv.smartplayer.presentation.theme.IptvColors
import com.iptv.smartplayer.presentation.theme.IptvDimens
import com.iptv.smartplayer.presentation.theme.LocalAccentColor
import com.iptv.smartplayer.presentation.theme.LocalIptvTypography

/**
 * شاشة تفاصيل الفيلم — Backdrop كامل العرض + معلومات + طاقم التمثيل + أعمال مشابهة،
 * بنفس بنية شاشة تفاصيل المسلسل تماماً للحفاظ على الاتساق البصري بين الشاشتين.
 */
@Composable
fun MovieDetailsScreen(
    onPlayClick: (Movie) -> Unit,
    onSimilarMovieClick: (Movie) -> Unit,
    viewModel: MovieDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val movie = state.movie

    when {
        state.isLoading && movie == null -> {
            ShimmerPlaceholder(modifier = Modifier.fillMaxSize())
        }
        state.errorMessage != null && movie == null -> {
            ErrorStateView(message = state.errorMessage.orEmpty(), modifier = Modifier.fillMaxSize(), onRetryClick = {})
        }
        movie != null -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(IptvColors.BackgroundPrimary),
                contentPadding = PaddingValues(bottom = 48.dp),
            ) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
                        AsyncImage(
                            model = movie.backdropUrl ?: movie.posterUrl,
                            contentDescription = movie.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                        Box(modifier = Modifier.fillMaxSize().background(verticalScrimBrush()))

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(horizontal = IptvDimens.tvOverscanMargin, vertical = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(movie.title, style = LocalIptvTypography.current.display, color = IptvColors.TextPrimary)

                            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                movie.rating?.let { RatingBadge(rating = it) }
                                movie.releaseYear?.take(4)?.let {
                                    Text(it, style = LocalIptvTypography.current.body, color = IptvColors.TextSecondary)
                                }
                                movie.durationMinutes?.let {
                                    Text("$it دقيقة", style = LocalIptvTypography.current.body, color = IptvColors.TextSecondary)
                                }
                            }

                            if (movie.genres.isNotEmpty()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    movie.genres.take(4).forEach { genre ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(IptvDimens.chipCornerRadius))
                                                .background(IptvColors.SurfaceElevated)
                                                .padding(horizontal = 12.dp, vertical = 6.dp),
                                        ) {
                                            Text(genre, style = LocalIptvTypography.current.caption, color = IptvColors.TextSecondary)
                                        }
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Button(
                                    onClick = { onPlayClick(movie) },
                                    colors = ButtonDefaults.buttonColors(containerColor = LocalAccentColor.current),
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                    androidx.compose.foundation.layout.Spacer(Modifier.width(8.dp))
                                    Text("تشغيل")
                                }
                                OutlinedButton(onClick = viewModel::toggleFavorite) {
                                    Icon(if (state.isFavorite) Icons.Filled.Check else Icons.Filled.Add, contentDescription = null)
                                    androidx.compose.foundation.layout.Spacer(Modifier.width(8.dp))
                                    Text(if (state.isFavorite) "في المفضلة" else "أضف للمفضلة")
                                }
                                OutlinedButton(onClick = { /* مشاركة */ }) {
                                    Icon(Icons.Filled.Share, contentDescription = null)
                                }
                            }
                        }
                    }
                }

                movie.overview?.let { overview ->
                    item {
                        Text(
                            text = overview,
                            style = LocalIptvTypography.current.body,
                            color = IptvColors.TextSecondary,
                            modifier = Modifier.padding(horizontal = IptvDimens.tvOverscanMargin, vertical = 20.dp),
                        )
                    }
                }

                if (movie.cast.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(bottom = 16.dp)) {
                            SectionHeaderRow(
                                title = "طاقم العمل",
                                modifier = Modifier.fillMaxWidth().padding(horizontal = IptvDimens.tvOverscanMargin, vertical = 8.dp),
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = IptvDimens.tvOverscanMargin),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                items(movie.cast) { member -> CastMemberCard(member) }
                            }
                        }
                    }
                }

                if (state.similar.isNotEmpty()) {
                    item {
                        Column {
                            SectionHeaderRow(
                                title = "أعمال مشابهة",
                                modifier = Modifier.fillMaxWidth().padding(horizontal = IptvDimens.tvOverscanMargin, vertical = 8.dp),
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = IptvDimens.tvOverscanMargin),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                items(state.similar) { similarMovie ->
                                    MoviePosterCard(movie = similarMovie, onClick = { onSimilarMovieClick(similarMovie) })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CastMemberCard(member: CastMember) {
    Column(
        modifier = Modifier.width(90.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model = member.profileUrl,
            contentDescription = member.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(80.dp).clip(CircleShape).background(IptvColors.SurfaceElevated),
        )
        androidx.compose.foundation.layout.Spacer(Modifier.height(6.dp))
        Text(
            member.name,
            style = LocalIptvTypography.current.caption,
            color = IptvColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        member.character?.let {
            Text(it, style = LocalIptvTypography.current.caption, color = IptvColors.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
