package com.assist_software.expenseappmvp.application.builder

import com.assist_software.expenseappmvp.BuildConfig
import com.assist_software.expenseappmvp.data.utils.rx.RxSchedulers
import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named

@Module
class RestServiceModule {

    @AppScope
    @Provides
    @Named("exchange")
    fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient, rxSchedulers: RxSchedulers): Retrofit {
        val url = BuildConfig.API_URL

        return Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(rxSchedulers.network()))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(url)
                .client(okHttpClient)
                .build()
    }

    @AppScope
    @Provides
    @Named("server")
    fun provideRetrofitServer(gson: Gson, okHttpClient: OkHttpClient, rxSchedulers: RxSchedulers): Retrofit {
        val url = BuildConfig.BALANCE_NOTIFICATION_URL

        return Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(rxSchedulers.network()))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(url)
                .client(okHttpClient)
                .build()
    }

    @AppScope
    @Provides
    fun provideAPIInterface(@Named("exchange") retrofit: Retrofit): RestServiceInterface {
        return retrofit.create(RestServiceInterface::class.java)
    }

    @AppScope
    @Provides
    fun provideAPIServiceInterface(@Named("server") retrofit: Retrofit): RestServiceNotification {
        return retrofit.create(RestServiceNotification::class.java)
    }
}
