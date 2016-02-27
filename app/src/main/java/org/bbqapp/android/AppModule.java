/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 bbqapp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.bbqapp.android;

import android.app.Application;
import android.content.Context;
import android.location.LocationManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.squareup.picasso.Picasso;

import org.bbqapp.android.api.PicassoPictureRequestTransformer;
import org.bbqapp.android.api.converter.IdConverterFactory;
import org.bbqapp.android.api.converter.LatLngConverterFactory;
import org.bbqapp.android.api.converter.LocationConverterFactory;
import org.bbqapp.android.api.converter.PictureConverterFactory;
import org.bbqapp.android.service.LocationService;
import org.bbqapp.android.api.service.PlaceService;
import org.bbqapp.android.service.GeocodeService;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.halfbit.tinybus.TinyBus;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Application module to hold global states
 */
@Module(library = true)
public class AppModule {
    private final App app;

    AppModule(App app) {
        this.app = app;
    }

    @Provides
    @Singleton
    Application provideApplication() {
        return app;
    }

    @Provides
    @Singleton
    LocationManager provideLocationManager(Application context) {
        return (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Provides
    @Singleton
    LocationService provideLocationService2(LocationManager locationManager) {
        return new LocationService(locationManager);
    }

    @Provides
    @Singleton
    GeocodeService provideGeocodeService(Application context) {
        return new GeocodeService(context);
    }

    @Provides
    @Singleton
    RefWatcher provideRefWatcher(Application application) {
        if (BuildConfig.DEBUG) {
            return RefWatcher.DISABLED;
        } else {
            return LeakCanary.install(application);
        }
    }

    @Provides
    @Singleton
    TinyBus provideTinyBus(Application context) {
        return new TinyBus(context);
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient() {
        return new OkHttpClient.Builder()
                .build();
    }

    @Provides
    @Singleton
    Picasso providePicasso(Application context, OkHttpClient client) {
        return new Picasso.Builder(context)
                .downloader(new OkHttp3Downloader(client))
                .requestTransformer(new PicassoPictureRequestTransformer(BuildConfig.API_URL))
                .build();
    }

    @Provides
    @Singleton
    ObjectMapper provideObjectMapper() {
        return new ObjectMapper().registerModule(new KotlinModule());
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit2(OkHttpClient client, ObjectMapper objectMapper) {
        return new Retrofit.Builder()
                .client(client)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(PictureConverterFactory.Companion.create())
                .addConverterFactory(IdConverterFactory.Companion.create())
                .addConverterFactory(LocationConverterFactory.Companion.create())
                .addConverterFactory(LatLngConverterFactory.Companion.create())
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .baseUrl(BuildConfig.API_HOST)
                .build();
    }

    @Provides
    @Singleton
    PlaceService providePlaceService(Retrofit retrofit) {
        return retrofit.create(org.bbqapp.android.api.service.PlaceService.class);
    }

    @Provides
    @Named("main")
    Scheduler provideMainScheduler() {
        return AndroidSchedulers.mainThread();
    }

    @Provides
    @Named("io")
    Scheduler provideIoScheduler() {
        return Schedulers.io();
    }
}
