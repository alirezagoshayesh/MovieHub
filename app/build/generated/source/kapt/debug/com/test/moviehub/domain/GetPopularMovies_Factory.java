// Generated by Dagger (https://dagger.dev).
package com.test.moviehub.domain;

import dagger.internal.Factory;
import javax.inject.Provider;

@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class GetPopularMovies_Factory implements Factory<GetPopularMovies> {
  private final Provider<Repository> repositoryProvider;

  public GetPopularMovies_Factory(Provider<Repository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetPopularMovies get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetPopularMovies_Factory create(Provider<Repository> repositoryProvider) {
    return new GetPopularMovies_Factory(repositoryProvider);
  }

  public static GetPopularMovies newInstance(Repository repository) {
    return new GetPopularMovies(repository);
  }
}