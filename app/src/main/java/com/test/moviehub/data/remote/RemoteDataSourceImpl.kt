package com.test.moviehub.data.remote

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.test.moviehub.data.SearchType
import com.test.moviehub.data.model.GetDetailsResponse
import com.test.moviehub.data.model.MovieResult
import com.test.moviehub.data.remote.connection.MService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RemoteDataSourceImpl @Inject constructor(private val service: MService) :
    RemoteDataSource {
    override suspend fun searchMovies(searchKeyword: String): Flow<PagingData<MovieResult>> {
        return Pager(
            config = PagingConfig(enablePlaceholders = true, pageSize = 10),
            pagingSourceFactory = {
                MoviesResultsDataSource(service, SearchType.Search, searchKeyword)
            }
        ).flow
    }

    override suspend fun getPopularMovies(): Flow<PagingData<MovieResult>> {
        return Pager(
            config = PagingConfig(enablePlaceholders = true, pageSize = 10),
            pagingSourceFactory = {
                MoviesResultsDataSource(service, SearchType.Popular)
            }
        ).flow
    }

    override suspend fun getDetails(movieId: Int): GetDetailsResponse {
        return service.getDetails(movieId)
    }

}