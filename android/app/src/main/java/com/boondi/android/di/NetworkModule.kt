package com.boondi.android.di

import com.boondi.android.BuildConfig
import com.boondi.android.data.remote.api.AuthApi
import com.boondi.android.data.remote.api.PostApi
import com.boondi.android.data.remote.api.TimelineApi
import com.boondi.android.data.remote.api.UserApi
import com.boondi.android.data.remote.interceptor.AuthInterceptor
import com.boondi.android.data.remote.interceptor.TokenAuthenticator
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/** Distinguishes the token-less client (auth endpoints) from the authenticated client. */
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class PlainClient
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class AuthClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    // ---- Plain client (public auth endpoints; no Bearer token, no 401 refresh) ----

    @Provides
    @Singleton
    @PlainClient
    fun providePlainClient(logging: HttpLoggingInterceptor): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @PlainClient
    fun providePlainRetrofit(@PlainClient client: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    fun provideAuthApi(@PlainClient retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    // ---- Authenticated client (everything else; adds Bearer + auto-refresh) ----

    @Provides
    @Singleton
    @AuthClient
    fun provideAuthClient(
        logging: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .authenticator(tokenAuthenticator)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @AuthClient
    fun provideAuthRetrofit(@AuthClient client: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    fun provideUserApi(@AuthClient retrofit: Retrofit): UserApi = retrofit.create(UserApi::class.java)

    @Provides
    @Singleton
    fun providePostApi(@AuthClient retrofit: Retrofit): PostApi = retrofit.create(PostApi::class.java)

    @Provides
    @Singleton
    fun provideTimelineApi(@AuthClient retrofit: Retrofit): TimelineApi =
        retrofit.create(TimelineApi::class.java)
}
