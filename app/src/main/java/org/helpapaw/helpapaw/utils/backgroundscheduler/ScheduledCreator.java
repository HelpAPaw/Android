package org.helpapaw.helpapaw.utils.backgroundscheduler;

import android.content.Context;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;
import com.evernote.android.job.JobManager;

/**
 * Created by Emil Ivanov on 11/20/2016.
 */

public class ScheduledCreator implements JobCreator {

    @Override
    public Job create(String tag) {
        switch (tag) {
            case SignalsSyncJob.TAG:
                return new SignalsSyncJob();
            default:
                return null;
        }
    }
    public static final class AddReceiver extends AddJobCreatorReceiver {
        @Override
        protected void addJobCreator(@NonNull Context context, @NonNull JobManager manager) {

            manager.addJobCreator(new ScheduledCreator());
        }
    }

}
