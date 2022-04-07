package org.helpapaw.helpapaw.data.user;

import android.util.Log;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessException;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.local.UserTokenStorageFactory;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.PawApplication;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by iliyan on 7/25/16
 */
public class BackendlessUserManager implements UserManager {

    private static final String USER_EMAIL_FIELD = "email";
    private static final String USER_NAME_FIELD = "name";
    private static final String USER_PHONE_NUMBER_FIELD = "phoneNumber";
    private static final String USER_ACCEPTED_PRIVACY_POLICY_FIELD = "acceptedPrivacyPolicy";

    @Override
    public void login(String email, String password, final LoginCallback loginCallback) {
        Backendless.UserService.login(email, password, new AsyncCallback<BackendlessUser>() {
            public void handleResponse(BackendlessUser user) {
                Backendless.UserService.setCurrentUser(user);
                loginCallback.onLoginSuccess(user.getUserId());

                FirebaseCrashlytics.getInstance().setUserId(user.getUserId());
            }

            public void handleFault(BackendlessFault fault) {
                loginCallback.onLoginFailure(fault.getMessage());
            }
        }, true);
    }

    @Override
    public  void loginWithFacebook(String accessToken, final LoginCallback loginCallback) {
        Backendless.UserService.loginWithOAuth2("facebook", accessToken, null, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser user) {
                Backendless.UserService.setCurrentUser(user);
                loginCallback.onLoginSuccess(user.getUserId());

                FirebaseCrashlytics.getInstance().setUserId(user.getUserId());
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                loginCallback.onLoginFailure(fault.getMessage());
            }
        },
        true);
    }

    @Override
    public  void loginWithGoogle(GoogleSignInAccount account, final LoginCallback loginCallback) {

        // From GoogleSignInAccount we get a server authorization code through which we obtain
        // the oAuth acccess token
        String authCode = account.getServerAuthCode();
        if (authCode == null) {
            loginCallback.onLoginFailure("Authorization code is null");
            return;
        }
        RequestBody requestBody = new FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("client_id", PawApplication.getContext().getResources().getString(R.string.google_oauth_client_id))
                .add("client_secret", PawApplication.getContext().getResources().getString(R.string.google_oauth_client_secret))
                .add("redirect_uri","")
                .add("code", authCode)
                .build();
        final Request request = new Request.Builder()
                .url("https://www.googleapis.com/oauth2/v4/token")
                .post(requestBody)
                .build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                loginCallback.onLoginFailure(e.getLocalizedMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    if (response.body() == null) {
                        loginCallback.onLoginFailure("Obtaining Google access token failed: Got empty response");
                        return;
                    }
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    String accessToken = jsonObject.getString("access_token");
                    // Now that we have the access token, we can log into Backendless
                    Backendless.UserService.loginWithOAuth2("googleplus", accessToken, null, new AsyncCallback<BackendlessUser>() {
                                @Override
                                public void handleResponse(BackendlessUser user) {
                                    Backendless.UserService.setCurrentUser(user);
                                    loginCallback.onLoginSuccess(user.getUserId());

                                    FirebaseCrashlytics.getInstance().setUserId(user.getUserId());
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    loginCallback.onLoginFailure(fault.getMessage());
                                }
                            },
                            true);
                } catch (Exception e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    loginCallback.onLoginFailure(e.getLocalizedMessage());
                }
            }
        });
    }

    @Override
    public void register(String email, String password, String name, String phoneNumber, final RegistrationCallback registrationCallback) {
        BackendlessUser user = new BackendlessUser();
        user.setProperty(USER_EMAIL_FIELD, email);
        user.setProperty(USER_NAME_FIELD, name);
        user.setProperty(USER_PHONE_NUMBER_FIELD, phoneNumber);
        user.setProperty(USER_ACCEPTED_PRIVACY_POLICY_FIELD, true);
        user.setPassword(password);

        Backendless.UserService.register(user, new AsyncCallback<BackendlessUser>() {
            public void handleResponse(BackendlessUser registeredUser) {
                registrationCallback.onRegistrationSuccess();
            }

            public void handleFault(BackendlessFault fault) {
                registrationCallback.onRegistrationFailure(fault.getMessage());
            }
        });
    }

    @Override
    public void resetPassword(String email, ResetPasswordCallback resetPasswordCallback) {
        Backendless.UserService.restorePassword(email, new AsyncCallback<Void>() {
            @Override
            public void handleResponse(Void response) {
                resetPasswordCallback.onResetPasswordSuccess();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                resetPasswordCallback.onResetPasswordFailure(fault.getMessage());
            }
        });
    }

    @Override
    public void logout(final LogoutCallback logoutCallback) {
        Backendless.UserService.logout(new AsyncCallback<Void>() {
            public void handleResponse(Void response) {
                LoginManager.getInstance().logOut();
                logoutCallback.onLogoutSuccess();
            }

            public void handleFault(BackendlessFault fault) {
                logoutCallback.onLogoutFailure(fault.getMessage());
            }
        });
    }

    @Override
    public void isLoggedIn(final LoginCallback loginCallback) {
        Backendless.UserService.isValidLogin(new AsyncCallback<Boolean>() {
            @Override
            public void handleResponse(Boolean isValidLogin) {
                if ((isValidLogin != null) && (isValidLogin)) {
                    if (Backendless.UserService.CurrentUser() == null) {
                        //https://support.backendless.com/t/userservice-currentuser-is-null-even-if-usertokenstoragefactory-retruns-token/3239
                        String currentUserId = Backendless.UserService.loggedInUser();
                        if (!currentUserId.equals("")) {
                            Backendless.UserService.findById(currentUserId, new AsyncCallback<BackendlessUser>() {
                                @Override
                                public void handleResponse(BackendlessUser currentUser) {
                                    if (currentUser != null) {
                                        Backendless.UserService.setCurrentUser(currentUser);
                                        loginCallback.onLoginSuccess(currentUserId);
                                    }
                                    else {
                                        loginCallback.onLoginFailure(null);
                                    }
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    loginCallback.onLoginFailure(fault.getMessage());
                                }
                            });
                        }
                    } else {
                        loginCallback.onLoginSuccess(Backendless.UserService.CurrentUser().getUserId());
                    }
                } else {
                    loginCallback.onLoginFailure(null);
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                loginCallback.onLoginFailure(fault.getMessage());
            }
        });
    }

    @Override
    public String getUserToken() {
       return UserTokenStorageFactory.instance().getStorage().get();
    }

    @Override
    public String getLoggedUserId() {
        return Backendless.UserService.loggedInUser();
    }

    @Override
    public boolean isLoggedIn() {
        String userToken = getUserToken();

        return userToken != null && !userToken.equals("");
    }

    @Override
    public void setHasAcceptedPrivacyPolicy(boolean value, final SetUserPropertyCallback setUserPropertyCallback) {
        try {
            BackendlessUser currentUser = Backendless.UserService.CurrentUser();
            if (currentUser == null) {
                setUserPropertyCallback.onFailure(PawApplication.getContext().getResources().getString(R.string.txt_logged_user_not_found));
                return;
            }

            Backendless.UserService.CurrentUser().setProperty(USER_ACCEPTED_PRIVACY_POLICY_FIELD, true);
            Backendless.UserService.update(Backendless.UserService.CurrentUser(), new AsyncCallback<BackendlessUser>() {
                @Override
                public void handleResponse(BackendlessUser response) {
                    setUserPropertyCallback.onSuccess();
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    FirebaseCrashlytics.getInstance().recordException(new Throwable(fault.toString()));
                    setUserPropertyCallback.onFailure(fault.getMessage());
                }
            });
        }
        catch (BackendlessException exception) {
            // update failed, to get the error code, call exception.getFault().getCode()
            FirebaseCrashlytics.getInstance().recordException(exception);
        }
    }

    @Override
    public void getHasAcceptedPrivacyPolicy(final GetUserPropertyCallback getUserPropertyCallback) {

        //https://support.backendless.com/t/userservice-currentuser-is-null-even-if-usertokenstoragefactory-retruns-token/3239
        String currentUserId = Backendless.UserService.loggedInUser();
        if (!currentUserId.equals("")) {
            Backendless.UserService.findById(currentUserId, new AsyncCallback<BackendlessUser>() {
                @Override
                public void handleResponse(BackendlessUser currentUser) {
                    if (currentUser == null) {
                        getUserPropertyCallback.onFailure(PawApplication.getContext().getResources().getString(R.string.txt_logged_user_not_found));
                        return;
                    }

                    Object result = false;
                    Object value = currentUser.getProperty(USER_ACCEPTED_PRIVACY_POLICY_FIELD);
                    if (value != null) {
                        result = value;
                    }
                    getUserPropertyCallback.onSuccess(result);
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    getUserPropertyCallback.onFailure(fault.getMessage());
                }
            });
        }
        else {
            getUserPropertyCallback.onFailure("User not logged in!");
        }
    }

    @Override
    public void getUserName(final GetUserPropertyCallback getUserPropertyCallback) {
        String currentUserId = Backendless.UserService.loggedInUser();
        if (!currentUserId.equals("")) {
            Backendless.UserService.findById(currentUserId, new AsyncCallback<BackendlessUser>() {
                @Override
                public void handleResponse(BackendlessUser currentUser) {
                    if (currentUser == null) {
                        getUserPropertyCallback.onFailure(PawApplication.getContext().getResources().getString(R.string.txt_logged_user_not_found));
                        return;
                    }

                    Object result = "";
                    Object value = currentUser.getProperty(USER_NAME_FIELD);
                    if (value != null) {
                        result = value;
                    }
                    getUserPropertyCallback.onSuccess(result);
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    getUserPropertyCallback.onFailure(fault.getMessage());
                }
            });
        } else {
            getUserPropertyCallback.onFailure("User not logged in!");
        }
    }

    @Override
    public void getUserPhone(final GetUserPropertyCallback getUserPropertyCallback) {
        String currentUserId = Backendless.UserService.loggedInUser();
        if (!currentUserId.equals("")) {
            Backendless.UserService.findById(currentUserId, new AsyncCallback<BackendlessUser>() {
                @Override
                public void handleResponse(BackendlessUser currentUser) {
                    if (currentUser == null) {
                        getUserPropertyCallback.onFailure(PawApplication.getContext().getResources().getString(R.string.txt_logged_user_not_found));
                        return;
                    }

                    Object result = "";
                    Object value = currentUser.getProperty(USER_PHONE_NUMBER_FIELD);
                    if (value != null) {
                        result = value;
                    }
                    getUserPropertyCallback.onSuccess(result);
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    getUserPropertyCallback.onFailure(fault.getMessage());
                }
            });
        } else {
            getUserPropertyCallback.onFailure("User not logged in!");
        }
    }
}
