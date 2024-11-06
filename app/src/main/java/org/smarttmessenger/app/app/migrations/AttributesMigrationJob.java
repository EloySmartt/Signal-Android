package com.smarttmessenger.app.migrations;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.signal.core.util.logging.Log;
import com.smarttmessenger.app.dependencies.AppDependencies;
import com.smarttmessenger.app.jobmanager.Job;
import com.smarttmessenger.app.jobs.RefreshAttributesJob;
import com.smarttmessenger.app.jobs.RefreshOwnProfileJob;

/**
 * Schedules a re-upload of the users attributes followed by a download of their profile.
 */
public final class AttributesMigrationJob extends MigrationJob {

  private static final String TAG = Log.tag(AttributesMigrationJob.class);

  public static final String KEY = "AttributesMigrationJob";

  AttributesMigrationJob() {
    this(new Parameters.Builder().build());
  }

  private AttributesMigrationJob(@NonNull Parameters parameters) {
    super(parameters);
  }

  @Override
  public boolean isUiBlocking() {
    return false;
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  @Override
  public void performMigration() {
    Log.i(TAG, "Scheduling attributes upload and profile refresh job chain");
    AppDependencies.getJobManager().startChain(new RefreshAttributesJob())
                   .then(new RefreshOwnProfileJob())
                   .enqueue();
  }

  @Override
  boolean shouldRetry(@NonNull Exception e) {
    return false;
  }

  public static class Factory implements Job.Factory<AttributesMigrationJob> {
    @Override
    public @NonNull AttributesMigrationJob create(@NonNull Parameters parameters, @Nullable byte[] serializedData) {
      return new AttributesMigrationJob(parameters);
    }
  }
}
