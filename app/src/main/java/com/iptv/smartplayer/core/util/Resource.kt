package com.iptv.smartplayer.core.util

/**
 * غلاف موحّد لحالة أي بيانات قادمة من Repository — يُستهلك مباشرة من الـ ViewModels
 * لعرض Skeleton/المحتوى/EmptyState/ErrorState دون منطق متكرر في كل شاشة.
 */
sealed class Resource<out T> {
    data class Loading<T>(val cachedData: T? = null) : Resource<T>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val message: String, val cachedData: T? = null) : Resource<T>()
}
