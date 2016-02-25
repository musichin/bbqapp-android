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

package org.bbqapp.android.service;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

import rx.Observable;
import rx.subjects.ReplaySubject;

/**
 * Geocoder and reverse geocoder
 */
public final class GeocodeService {
    private static final int MAX_RESULTS_DEFAULT_VALUE = 1;

    private Context context;

    public GeocodeService(Context context) {
        this.context = context;
    }

    public Observable<Address> resolve(Location location) {
        return resolve(location, MAX_RESULTS_DEFAULT_VALUE);
    }

    public Observable<Address> resolve(LatLng location, int maxResults) {
        final ReplaySubject<Address> subject = ReplaySubject.createWithSize(maxResults);

        try {
            for (Address address : geocode(location, maxResults)) {
                subject.onNext(address);
            }
            subject.onCompleted();
        } catch (IOException e) {
            subject.onError(e);
        }

        return subject;
    }

    public Observable<Address> resolve(Location location, int maxResults) {
        return resolve(toLatLng(location), maxResults);
    }

    public Observable<Address> resolve(String location) {
        return resolve(location, MAX_RESULTS_DEFAULT_VALUE);
    }

    public Observable<Address> resolve(String location, int maxResults) {
        final ReplaySubject<Address> subject = ReplaySubject.createWithSize(maxResults);

        try {
            for (Address address : geocode(location, maxResults)) {
                subject.onNext(address);
            }
            subject.onCompleted();
        } catch (IOException e) {
            subject.onError(e);
        }

        return subject;
    }

    private Geocoder createGeocoder() {
        return new Geocoder(context);
    }

    private List<Address> geocode(String location, int maxResults) throws IOException {
        Geocoder geocoder = createGeocoder();
        return geocoder.getFromLocationName(location, maxResults);
    }

    private List<Address> geocode(LatLng location, int maxResults) throws IOException {
        Geocoder geocoder = createGeocoder();
        return geocoder.getFromLocation(location.latitude, location.longitude, maxResults);
    }

    private static LatLng toLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }
}
