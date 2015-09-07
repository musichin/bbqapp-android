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

package org.bbqapp.android.view.list;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import org.bbqapp.android.R;
import org.bbqapp.android.api.Callback;
import org.bbqapp.android.api.exception.ApiException;
import org.bbqapp.android.api.model.Place;
import org.bbqapp.android.api.service.Places;
import org.bbqapp.android.service.LocationService;
import org.bbqapp.android.view.BaseFragment;

import java.util.List;

import javax.inject.Inject;

/**
 * Fragment to display places in a clickable list
 */
public class ListFragment extends BaseFragment implements LocationService.OnLocationListener {

    private static final String TAG = ListFragment.class.getName();

    private ListView placeList;
    private PlaceListAdapter placeAdapter;

    @Inject
    LayoutInflater layoutInflater;

    @Inject
    LocationService locationService;

    @Inject
    Places placesEP;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (placeList == null) {
            placeList = (ListView) inflater.inflate(R.layout.view_list, container, false);
        }

        return placeList;
    }

    @Override
    public void onResume() {
        super.onResume();

        placeAdapter = new PlaceListAdapter(layoutInflater);
        placeList.setAdapter(placeAdapter);
        Location l = locationService.getLocation();
        placeAdapter.setLocation(l);

        getActivity().setTitle(R.string.menu_list);
        locationService.addOnLocationListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        locationService.removeOnLocationListener(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        placeAdapter.setLocation(location);

        if (placeAdapter.getList() == null) {
            placesEP.getPlaces(location.getLatitude() + "," + location.getLongitude(), 10000, new Callback<List<Place>>
                    () {
                @Override
                public void onSuccess(List<Place> places) {
                    placeAdapter.setPlaces(places);
                }

                @Override
                public void onFailure(ApiException cause) {
                    Toast.makeText(getActivity(), "Could not retrieve places: " + cause.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
