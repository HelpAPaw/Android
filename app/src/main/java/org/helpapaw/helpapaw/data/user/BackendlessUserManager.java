package org.helpapaw.helpapaw.data.user;

import android.util.Log;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessException;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.local.UserTokenStorageFactory;
import com.facebook.login.LoginManager;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

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
        Backendless.UserService.loginWithFacebookSdk(accessToken, new AsyncCallback<BackendlessUser>() {
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
    public boolean isLoggedIn() {
        String userToken = getUserToken();

        return userToken != null && !userToken.equals("");
    }

    @Override
    public void setHasAcceptedPrivacyPolicy(boolean value, final SetUserPropertyCallback setUserPropertyCallback) {
        try
        {
            Backendless.UserService.CurrentUser().setProperty(USER_ACCEPTED_PRIVACY_POLICY_FIELD, true);
            Backendless.UserService.update(Backendless.UserService.CurrentUser(), new AsyncCallback<BackendlessUser>() {
                @Override
                public void handleResponse(BackendlessUser response) {
                    setUserPropertyCallback.onSuccess();
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    setUserPropertyCallback.onFailure(fault.getMessage());
                }
            });
        }
        catch( BackendlessException exception )
        {
            // update failed, to get the error code, call exception.getFault().getCode()
            Log.e(BackendlessUserManager.class.getSimpleName(), exception.getMessage());
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
