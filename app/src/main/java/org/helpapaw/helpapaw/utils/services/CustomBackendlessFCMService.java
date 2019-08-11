package org.helpapaw.helpapaw.utils.services;

import android.content.Context;
import android.content.Intent;

import com.backendless.push.BackendlessFCMService;

public class CustomBackendlessFCMService extends BackendlessFCMService {

    @Override
    public boolean onMessage(Context appContext, Intent msgIntent )
    {
//        Log.d( "BackendlessFCMService", msgIntent.getExtras().toString() );
        return true;
    }
}
