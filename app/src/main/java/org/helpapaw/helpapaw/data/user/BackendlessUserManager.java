package org.helpapaw.helpapaw.data.user;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

/**
 * Created by iliyan on 7/25/16
 */
public class BackendlessUserManager implements UserManager {

    private static final String USER_EMAIL_FIELD = "email";
    private static final String USER_NAME_FIELD = "name";
    private static final String USER_PHONE_NUMBER_FIELD = "phoneNumber";

    @Override
    public void login(String email, String password, final LoginCallback loginCallback) {
        Backendless.UserService.login(email, password, new AsyncCallback<BackendlessUser>() {
            public void handleResponse(BackendlessUser user) {
                loginCallback.onLoginSuccess();
            }

            public void handleFault(BackendlessFault fault) {
                loginCallback.onLoginFailure(fault.getMessage());
            }
        }, true);
    }

    @Override
    public void register(String email, String password, String name, String phoneNumber, final RegistrationCallback registrationCallback) {
        BackendlessUser user = new BackendlessUser();
        user.setProperty(USER_EMAIL_FIELD, email);
        user.setProperty(USER_NAME_FIELD, name);
        user.setProperty(USER_PHONE_NUMBER_FIELD, phoneNumber);
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
                if (isValidLogin) {
                    if (Backendless.UserService.CurrentUser() == null) {
                        String currentUserId = Backendless.UserService.loggedInUser();
                        if (!currentUserId.equals("")) {
                            Backendless.UserService.findById(currentUserId, new AsyncCallback<BackendlessUser>() {
                                @Override
                                public void handleResponse(BackendlessUser currentUser) {
                                    Backendless.UserService.setCurrentUser(currentUser);
                                    loginCallback.onLoginSuccess();
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    loginCallback.onLoginFailure(fault.getMessage());
                                }
                            });
                        }
                    } else {
                        loginCallback.onLoginSuccess();
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
}
