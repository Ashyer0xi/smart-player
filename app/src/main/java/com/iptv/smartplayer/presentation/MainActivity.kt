package com.iptv.smartplayer

import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.iptv.smartplayer.presentation.auth.LoginScreen
import com.iptv.smartplayer.presentation.favorites.FavoritesScreen
import com.iptv.smartplayer.presentation.home.HomeScreen
import com.iptv.smartplayer.presentation.live.LiveTvScreen
import com.iptv.smartplayer.presentation.movies.MovieDetailsScreen
import com.iptv.smartplayer.presentation.movies.MoviesScreen
import com.iptv.smartplayer.presentation.navigation.Screen
import com.iptv.smartplayer.presentation.navigation.StartDestinationViewModel
import com.iptv.smartplayer.presentation.player.PlayerScreen
import com.iptv.smartplayer.presentation.series.SeriesDetailsScreen
import com.iptv.smartplayer.presentation.series.SeriesScreen
import com.iptv.smartplayer.presentation.settings.SettingsScreen
import com.iptv.smartplayer.presentation.settings.SettingsViewModel
import com.iptv.smartplayer.presentation.theme.IptvColors
import com.iptv.smartplayer.presentation.theme.IptvSmartPlayerTheme
import dagger.hilt.android.AndroidEntryPoint

// النشاط الرئيسي الوحيد بالتطبيق (نمط Single-Activity)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val isTvDevice: Boolean
        get() = (getSystemService(UI_MODE_SERVICE) as android.app.UiModeManager)
            .currentModeType == Configuration.UI_MODE_TYPE_TELEVISION ||
            packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val accentColor by settingsViewModel.accentColor.collectAsState()

            IptvSmartPlayerTheme(isTv = isTvDevice, accentColor = accentColor) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AppRoot()
                }
            }
        }
    }
}

/**
 * ينتظر قرار شاشة الانطلاق (تسجيل دخول أم رئيسية مباشرة) قبل إنشاء NavHost،
 * لأن startDestination يجب أن يكون معروفاً عند إنشاء NavHost لأول مرة.
 */
@Composable
private fun AppRoot(startDestinationViewModel: StartDestinationViewModel = hiltViewModel()) {
    val startRoute by startDestinationViewModel.startRoute.collectAsState()

    when (val route = startRoute) {
        null -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = IptvColors.TextPrimary)
        }
        else -> AppNavHost(navController = rememberNavController(), startDestination = route)
    }
}

@Composable
fun AppNavHost(navController: NavHostController, startDestination: String = Screen.Home.route) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onMovieClick = { movie -> navController.navigate(Screen.MovieDetails.createRoute(movie.xtreamStreamId)) },
                onPlayClick = { movie ->
                    navController.navigate(Screen.Player.createRoute("vod", movie.xtreamStreamId.toString()))
                },
                onContinueWatchingClick = { entry ->
                    navController.navigate(Screen.Player.createRoute("vod", entry.contentId))
                },
            )
        }
        composable(Screen.Movies.route) {
            MoviesScreen(
                onMovieClick = { movie -> navController.navigate(Screen.MovieDetails.createRoute(movie.xtreamStreamId)) },
            )
        }
        composable(
            route = Screen.MovieDetails.route,
            arguments = listOf(navArgument(Screen.MovieDetails.ARG_MOVIE_ID) { type = androidx.navigation.NavType.IntType }),
        ) {
            MovieDetailsScreen(
                onPlayClick = { movie -> navController.navigate(Screen.Player.createRoute("vod", movie.xtreamStreamId.toString())) },
                onSimilarMovieClick = { movie -> navController.navigate(Screen.MovieDetails.createRoute(movie.xtreamStreamId)) },
            )
        }
        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onFavoriteClick = { favorite ->
                    val type = if (favorite.contentType == com.iptv.smartplayer.data.local.entity.ContentType.SERIES) "series" else "vod"
                    navController.navigate(Screen.Player.createRoute(type, favorite.contentId))
                },
            )
        }
        composable(Screen.Series.route) {
            SeriesScreen(
                onSeriesClick = { series -> navController.navigate(Screen.SeriesDetails.createRoute(series.xtreamSeriesId)) },
            )
        }
        composable(
            route = Screen.SeriesDetails.route,
            arguments = listOf(navArgument(Screen.SeriesDetails.ARG_SERIES_ID) { type = androidx.navigation.NavType.IntType }),
        ) {
            SeriesDetailsScreen(
                onEpisodeClick = { episode ->
                    navController.navigate(Screen.Player.createRoute("vod", episode.id))
                },
            )
        }
        composable(Screen.LiveTv.route) {
            LiveTvScreen(
                onChannelPlay = { channel ->
                    navController.navigate(Screen.Player.createRoute("live", channel.streamId.toString()))
                },
            )
        }
        composable(Screen.Settings.route) { SettingsScreen() }
        composable(
            route = Screen.Player.route,
            arguments = listOf(
                navArgument(Screen.Player.ARG_CONTENT_TYPE) { type = androidx.navigation.NavType.StringType },
                navArgument(Screen.Player.ARG_CONTENT_ID) { type = androidx.navigation.NavType.StringType },
            ),
        ) {
            PlayerScreen(onBackClick = { navController.popBackStack() })
        }
    }
}
