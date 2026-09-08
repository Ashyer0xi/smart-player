package com.iptv.smartplayer.presentation.series

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.iptv.smartplayer.domain.model.Series
import com.iptv.smartplayer.presentation.components.EmptyStateView
import com.iptv.smartplayer.presentation.components.ErrorStateView
import com.iptv.smartplayer.presentation.components.FocusableCard
import com.iptv.smartplayer.presentation.components.ShimmerPlaceholder
import com.iptv.smartplayer.presentation.theme.IptvColors
import com.iptv.smartplayer.presentation.theme.IptvDimens

/** شاشة قائمة المسلسلات — نفس بنية شاشة الأفلام (فلاتر + شبكة) */
@Composable
fun SeriesScreen(
    onSeriesClick: (Series) -> Unit,
    viewModel: SeriesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IptvColors.BackgroundPrimary)
            .padding(horizontal = IptvDimens.tvOverscanMargin, vertical = 16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            state.categories.take(8).forEach { category ->
                androidx.compose.material3.Text(
                    text = category.name,
                    color = if (state.selectedCategoryId == category.id) {
                        com.iptv.smartplayer.presentation.theme.LocalAccentColor.current
                    } else {
                        IptvColors.TextSecondary
                    },
                    style = com.iptv.smartplayer.presentation.theme.LocalIptvTypography.current.body,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
        }

        androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))

        when {
            state.isLoading && state.series.isEmpty() -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = IptvDimens.posterCardWidthTv),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(12) {
                        ShimmerPlaceholder(modifier = Modifier.height(IptvDimens.posterCardHeightTv).fillMaxWidth())
                    }
                }
            }
            state.errorMessage != null && state.series.isEmpty() -> {
                ErrorStateView(
                    message = state.errorMessage.orEmpty(),
                    modifier = Modifier.fillMaxSize(),
                    onRetryClick = { viewModel.onCategorySelected(state.selectedCategoryId) },
                )
            }
            state.series.isEmpty() -> {
                EmptyStateView(title = "لا مسلسلات", message = "لا يوجد محتوى في هذا التصنيف", modifier = Modifier.fillMaxSize())
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = IptvDimens.posterCardWidthTv),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                ) {
                    items(state.series) { series ->
                        SeriesPosterCard(series = series, onClick = { onSeriesClick(series) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SeriesPosterCard(series: Series, onClick: () -> Unit) {
    FocusableCard(
        onClick = onClick,
        modifier = Modifier.height(IptvDimens.posterCardHeightTv).fillMaxWidth(),
    ) { _ ->
        coil.compose.AsyncImage(
            model = series.posterUrl,
            contentDescription = series.title,
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
