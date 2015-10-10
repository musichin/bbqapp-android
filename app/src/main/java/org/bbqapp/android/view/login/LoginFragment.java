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

package org.bbqapp.android.view.login;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.SignInButton;

import org.bbqapp.android.R;
import org.bbqapp.android.auth.AuthCancel;
import org.bbqapp.android.auth.AuthData;
import org.bbqapp.android.auth.AuthError;
import org.bbqapp.android.auth.AuthEvent;
import org.bbqapp.android.auth.AuthInit;
import org.bbqapp.android.auth.Facebook;
import org.bbqapp.android.auth.GooglePlus;
import org.bbqapp.android.view.BaseFragment;
import org.bbqapp.android.view.LoginManager;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.halfbit.tinybus.Subscribe;

/**
 * Fragment for user login operations
 */
public class LoginFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = LoginFragment.class.getName();

    @Inject
    LoginManager loginManager;

    @Inject
    Activity activity;

    @Bind(R.id.login_buttons)
    LinearLayout loginButtons;

    @Bind(R.id.login_info)
    LinearLayout loginInfo;

    private boolean initialized = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_login, container, false);

        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.menu_login);

        loginButtons.setVisibility(View.GONE);
        loginInfo.setVisibility(View.GONE);

        initOrUpdate();
    }

    @Override
    public void onPause() {
        super.onPause();

        loginButtons.removeAllViews();

        initialized = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    private void initOrUpdate() {
        AuthData authData = loginManager.getLastAuthData();
        boolean loggedIn = authData != null && authData.isLoggedIn();

        if (!initialized && !loginManager.isBusy() && !loggedIn) {
            initialized = true;
            loginManager.init();
        } else {
            loginButtons.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
            loginInfo.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        }
    }

    @OnClick(R.id.logout_button)
    protected void logout() {
        loginManager.logout();
    }

    @Subscribe
    public void onAuthEvent(AuthEvent authEvent) {
        initOrUpdate();
    }

    @Subscribe
    public void onAuthError(AuthError authError) {
        onAuthEvent(authError);
    }

    @Subscribe
    public void onAuthCancel(AuthCancel authCancel) {
        onAuthEvent(authCancel);
    }

    @Subscribe
    public void onAuthData(AuthData authData) {
        onAuthEvent(authData);
    }

    @Subscribe
    public void onAuthInit(AuthInit authInit) {
        View button;
        switch (authInit.getAuthServiceId()) {
            case GooglePlus.ID:
                button = new SignInButton(activity);
                break;
            case Facebook.ID:
                button = new LoginButton(activity);
                break;
            default:
                button = null;
        }

        if (button != null) {
            button.setOnClickListener(this);
            loginButtons.addView(button);
        }

        onAuthEvent(authInit);
    }

    @Override
    public void onClick(View v) {
        if (v instanceof SignInButton) {
            loginManager.login(GooglePlus.ID);
        } else if (v instanceof LoginButton) {
            loginManager.login(Facebook.ID);
        }
    }
}
