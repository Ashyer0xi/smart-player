package com.iptv.smartplayer.presentation.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.iptv.smartplayer.data.local.entity.ContentType
import com.iptv.smartplayer.data.local.entity.FavoriteEntity
import com.iptv.smartplayer.presentation.components.EmptyStateView
import com.iptv.smartplayer.presentation.components.FocusableCard
import com.iptv.smartplayer.presentation.theme.IptvColors
import com.iptv.smartplayer.presentation.theme.IptvDimens

/** شاشة المفضلة — شبكة بوسترات لكل ما أضافه المستخدم من أفلام/مسلسلات/قنوات */
@Composable
fun FavoritesScreen(
    onFavoriteClick: (FavoriteEntity) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val favorites by viewModel.favorites.collectAsState()

    if (favorites.isEmpty()) {
        EmptyStateView(
            title = "لا مفضلة بعد",
            message = "أضف أفلاماً أو مسلسلات أو قنوات إلى المفضلة لتظهر هنا",
            modifier = Modifier.fillMaxSize().background(IptvColors.BackgroundPrimary),
        )
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = IptvDimens.posterCardWidthTv),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(IptvDimens.tvOverscanMargin),
            modifier = Modifier.fillMaxSize().background(IptvColors.BackgroundPrimary),
        ) {
            items(favorites) { favorite ->
                FocusableCard(
                    onClick = { onFavoriteClick(favorite) },
                    modifier = Modifier.height(IptvDimens.posterCardHeightTv),
                ) { _ ->
                    AsyncImage(
                        model = favorite.posterUrl,
                        contentDescription = favorite.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
