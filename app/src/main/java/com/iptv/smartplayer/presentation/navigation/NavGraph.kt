package com.iptv.smartplayer.presentation.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * تعريف كل مسارات التنقل في التطبيق في مكان واحد لتفادي أخطاء الطباعة اليدوية للمسارات
 * وتسهيل تمرير المعرّفات (IDs) عبر الشاشات.
 */
sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Home : Screen("home")
    data object Movies : Screen("movies")
    data object Series : Screen("series")
    data object LiveTv : Screen("live_tv")
    data object Settings : Screen("settings")
    data object Favorites : Screen("favorites")

    data object MovieDetails : Screen("movie_details/{movieId}") {
        fun createRoute(movieId: Int) = "movie_details/$movieId"
        const val ARG_MOVIE_ID = "movieId"
    }

    data object SeriesDetails : Screen("series_details/{seriesId}") {
        fun createRoute(seriesId: Int) = "series_details/$seriesId"
        const val ARG_SERIES_ID = "seriesId"
    }

    // Player route now supports an optional query parameter `next` containing the next item's id/uri (URL encoded)
    data object Player : Screen("player/{contentType}/{contentId}?next={next}") {
        const val ARG_CONTENT_TYPE = "contentType"
        const val ARG_CONTENT_ID = "contentId"
        const val ARG_NEXT = "next"

        fun createRoute(contentType: String, contentId: String, next: String? = null): String {
            return if (next.isNullOrBlank()) {
                "player/$contentType/$contentId"
            } else {
                val encoded = URLEncoder.encode(next, StandardCharsets.UTF_8.toString())
                "player/$contentType/$contentId?next=$encoded"
            }
        }
    }
}

/** تصنيفات شريط التنقل العلوي الرئيسي — تُستخدم في الرئيسية وTopBar المشترك */
enum class TopLevelDestination(val screen: Screen, val label: String) {
    HOME(Screen.Home, "الرئيسية"),
    LIVE(Screen.LiveTv, "القنوات المباشرة"),
    MOVIES(Screen.Movies, "أفلام"),
    SERIES(Screen.Series, "مسلسلات"),
    FAVORITES(Screen.Favorites, "المفضلة"),
    SETTINGS(Screen.Settings, "الإعدادات"),
}
