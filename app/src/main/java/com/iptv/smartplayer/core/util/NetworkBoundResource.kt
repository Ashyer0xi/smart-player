package com.iptv.smartplayer.core.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * نمط NetworkBoundResource — يطبّق استراتيجية "اعرض الكاش فوراً ثم حدّثه من الشبكة"
 * المذكورة في البرومبت لكل الـ Repositories (Xtream و TMDB).
 *
 * @param query يقرأ البيانات المحلية الحالية من Room كـ Flow.
 * @param shouldFetch يقرر إن كان يجب تحديث الكاش من الشبكة (مثلاً: فارغ أو منتهي الصلاحية).
 * @param fetch يجلب البيانات من الشبكة (Xtream/TMDB API).
 * @param saveFetchResult يحفظ نتيجة الشبكة في Room.
 * @param onFetchFailed يُستدعى عند فشل الشبكة (Timeout/404/500) لتسجيل الخطأ دون كسر تدفق الكاش المحلي.
 */
inline fun <ResultType, RequestType> networkBoundResource(
    crossinline query: () -> Flow<ResultType>,
    crossinline shouldFetch: (ResultType) -> Boolean = { true },
    crossinline fetch: suspend () -> RequestType,
    crossinline saveFetchResult: suspend (RequestType) -> Unit,
    crossinline onFetchFailed: (Throwable) -> Unit = {},
): Flow<Resource<ResultType>> = flow {
    val localData = query().first()

    if (shouldFetch(localData)) {
        emit(Resource.Loading(cachedData = localData))
        try {
            val fetchedResult = fetch()
            saveFetchResult(fetchedResult)
            query().map { Resource.Success(it) as Resource<ResultType> }
                .collect { emit(it) }
        } catch (throwable: IOException) {
            // فشل شبكة (لا اتصال، Timeout) — نعرض الكاش المتوفر مع رسالة خطأ واضحة بدل شاشة فارغة
            onFetchFailed(throwable)
            query().map { Resource.Error("تعذر الاتصال بالخادم، يتم عرض آخر بيانات محفوظة", it) as Resource<ResultType> }
                .collect { emit(it) }
        } catch (throwable: Exception) {
            onFetchFailed(throwable)
            query().map { Resource.Error(throwable.message ?: "حدث خطأ غير متوقع", it) as Resource<ResultType> }
                .collect { emit(it) }
        }
    } else {
        query().map { Resource.Success(it) as Resource<ResultType> }
            .collect { emit(it) }
    }
}
