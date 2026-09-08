package com.iptv.smartplayer.domain.usecase

import com.iptv.smartplayer.core.util.Resource
import com.iptv.smartplayer.domain.model.Movie
import com.iptv.smartplayer.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** يجلب قائمة الأفلام حسب التصنيف (أو الكل عند تركه فارغاً) */
class GetMoviesUseCase @Inject constructor(
    private val repository: MovieRepository,
) {
    operator fun invoke(categoryId: String? = null): Flow<Resource<List<Movie>>> =
        repository.getMovies(categoryId)
}

/** يجلب الأعمال الرائجة هذا الأسبوع من TMDB — يُستخدم في صف "الأكثر رواجاً" بالرئيسية */
class GetTrendingMoviesUseCase @Inject constructor(
    private val repository: MovieRepository,
) {
    operator fun invoke(): Flow<Resource<List<Movie>>> = repository.getTrendingMovies()
}

/** يجلب التفاصيل الكاملة لفيلم واحد (مدمجة من Xtream + TMDB) لعرضها في شاشة التفاصيل */
class GetMovieDetailsUseCase @Inject constructor(
    private val repository: MovieRepository,
) {
    operator fun invoke(streamId: Int): Flow<Resource<Movie>> = repository.getMovieDetails(streamId)
}

/** بحث فوري ضمن مكتبة الأفلام */
class SearchMoviesUseCase @Inject constructor(
    private val repository: MovieRepository,
) {
    operator fun invoke(query: String): Flow<Resource<List<Movie>>> = repository.searchMovies(query)
}
