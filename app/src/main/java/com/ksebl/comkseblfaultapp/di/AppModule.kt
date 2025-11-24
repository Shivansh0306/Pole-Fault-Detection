package com.ksebl.comkseblfaultapp.di

import com.ksebl.comkseblfaultapp.BuildConfig
import com.ksebl.comkseblfaultapp.network.ApiService
import com.ksebl.comkseblfaultapp.repository.MainRepository
import com.ksebl.comkseblfaultapp.viewmodel.MainViewModel
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit

val appModule = module {
    // Network
    single { provideOkHttpClient() }
    single { provideMoshi() }
    single { provideRetrofit(get(), get()) }
    single { provideApiService(get()) }
    
    // Repository
    single { MainRepository(get()) }
    
    // ViewModels
    viewModel { MainViewModel(get()) }
}

private fun provideOkHttpClient(): OkHttpClient {
    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
    val apiKeyInterceptor = Interceptor { chain ->
        val original = chain.request()
        val newRequest = original.newBuilder()
            .header("X-API-Key", BuildConfig.API_KEY)
            .build()
        chain.proceed(newRequest)
    }

    return OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(apiKeyInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
}

private fun provideMoshi(): Moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
    return Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
}

private fun provideApiService(retrofit: Retrofit): ApiService {
    return retrofit.create(ApiService::class.java)
}
