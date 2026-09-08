package com.iptv.smartplayer.presentation.movies

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.iptv.smartplayer.domain.model.Movie
import com.iptv.smartplayer.presentation.components.EmptyStateView
import com.iptv.smartplayer.presentation.components.ErrorStateView
import com.iptv.smartplayer.presentation.components.ShimmerPlaceholder
import com.iptv.smartplayer.presentation.home.MoviePosterCard
import com.iptv.smartplayer.presentation.theme.IptvColors
import com.iptv.smartplayer.presentation.theme.IptvDimens
import com.iptv.smartplayer.presentation.theme.LocalAccentColor
import com.iptv.smartplayer.presentation.theme.LocalIptvTypography

/**
 * شاشة الأفلام — شريط فلاتر علوي ثابت + بحث فوري + شبكة/قائمة قابلة للتبديل.
 */
@Composable
fun MoviesScreen(
    onMovieClick: (Movie) -> Unit,
    viewModel: MoviesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IptvColors.BackgroundPrimary)
            .padding(horizontal = IptvDimens.tvOverscanMargin, vertical = 16.dp),
    ) {
        // شريط الفلاتر + البحث + تبديل العرض
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (state.isSearchActive) {
                TextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    placeholder = { Text("ابحث عن فيلم...") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = IptvColors.SurfaceElevated,
                        unfocusedContainerColor = IptvColors.SurfaceDefault,
                    ),
                    modifier = Modifier.weight(1f),
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilterChip(label = "الكل", selected = state.selectedCategoryId == null) {
                        viewModel.onCategorySelected(null)
                    }
                    state.categories.take(6).forEach { category ->
                        FilterChip(
                            label = category.name,
                            selected = state.selectedCategoryId == category.id,
                        ) { viewModel.onCategorySelected(category.id) }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = { viewModel.onSearchActiveChanged(!state.isSearchActive) }) {
                    Icon(Icons.Filled.Search, contentDescription = "بحث", tint = IptvColors.TextPrimary)
                }
                IconButton(onClick = viewModel::onViewModeToggle) {
                    Icon(
                        imageVector = if (state.viewMode == ViewMode.GRID) Icons.Filled.List else Icons.Filled.GridView,
                        contentDescription = "تبديل العرض",
                        tint = IptvColors.TextPrimary,
                    )
                }
            }
        }

        Box(modifier = Modifier.height(12.dp))

        // ترتيب سريع
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SortOption.entries.forEach { option ->
                FilterChip(label = option.label, selected = state.sortOption == option) {
                    viewModel.onSortSelected(option)
                }
            }
        }

        Box(modifier = Modifier.height(16.dp))

        when {
            state.isLoading && state.movies.isEmpty() -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = IptvDimens.posterCardWidthTv),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(12) {
                        ShimmerPlaceholder(
                            modifier = Modifier.height(IptvDimens.posterCardHeightTv).fillMaxWidth(),
                        )
                    }
                }
            }
            state.errorMessage != null && state.movies.isEmpty() -> {
                ErrorStateView(
                    message = state.errorMessage.orEmpty(),
                    modifier = Modifier.fillMaxSize(),
                    onRetryClick = { viewModel.onCategorySelected(state.selectedCategoryId) },
                )
            }
            state.movies.isEmpty() -> {
                EmptyStateView(
                    title = "لا نتائج",
                    message = "لم يتم العثور على أفلام مطابقة",
                    modifier = Modifier.fillMaxSize(),
                )
            }
            state.viewMode == ViewMode.GRID -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = IptvDimens.posterCardWidthTv),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                ) {
                    items(state.movies) { movie ->
                        MoviePosterCard(movie = movie, onClick = { onMovieClick(movie) })
                    }
                }
            }
            else -> {
                androidx.compose.foundation.lazy.LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                ) {
                    items(state.movies) { movie ->
                        MovieListRow(movie = movie, onClick = { onMovieClick(movie) })
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(IptvDimens.chipCornerRadius))
            .background(if (selected) LocalAccentColor.current else IptvColors.SurfaceElevated)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .then(Modifier)
            .let { it }
            .clickable(onClick),
    ) {
        Text(
            text = label,
            style = LocalIptvTypography.current.caption,
            color = if (selected) IptvColors.TextPrimary else IptvColors.TextSecondary,
        )
    }
}

private fun Modifier.clickable(onClick: () -> Unit): Modifier =
    this.then(androidx.compose.foundation.clickable(onClick = onClick))

@Composable
private fun MovieListRow(movie: Movie, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(IptvDimens.cardCornerRadius))
            .background(IptvColors.SurfaceDefault)
            .clickable(onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        coil.compose.AsyncImage(
            model = movie.posterUrl,
            contentDescription = movie.title,
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier
                .height(100.dp)
                .clip(RoundedCornerShape(8.dp)),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(movie.title, style = LocalIptvTypography.current.title, color = IptvColors.TextPrimary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                movie.releaseYear?.take(4)?.let { Text(it, style = LocalIptvTypography.current.caption, color = IptvColors.TextSecondary) }
                movie.rating?.let { Text("⭐ %.1f".format(it), style = LocalIptvTypography.current.caption, color = IptvColors.TextSecondary) }
            }
            movie.overview?.let {
                Text(
                    it,
                    style = LocalIptvTypography.current.caption,
                    color = IptvColors.TextSecondary,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
            }
        }
    }
}
