package org.helpapaw.helpapaw.authentication;

public interface PrivacyPolicyConfirmationContract {

    interface Obtain {
        void onPrivacyPolicyObtained(String privacyPolicy);
    }

    interface UserResponse {
        void onUserAcceptedPrivacyPolicy();
        void onUserDeclinedPrivacyPolicy();
    }
}
