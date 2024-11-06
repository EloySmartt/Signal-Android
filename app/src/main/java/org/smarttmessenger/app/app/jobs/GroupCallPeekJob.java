package com.smarttmessenger.app.jobs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.smarttmessenger.app.dependencies.AppDependencies;
import com.smarttmessenger.app.jobmanager.Job;
import com.smarttmessenger.app.jobmanager.JobManager;
import com.smarttmessenger.app.jobmanager.JsonJobData;
import com.smarttmessenger.app.jobmanager.impl.DecryptionsDrainedConstraint;
import com.smarttmessenger.app.jobs.protos.GroupCallPeekJobData;

import java.io.IOException;

/**
 * Allows the enqueueing of one peek operation per group while the web socket is not drained.
 */
public final class GroupCallPeekJob extends BaseJob {

  public static final String KEY = "GroupCallPeekJob";

  private static final String QUEUE = "__GroupCallPeekJob__";

  @NonNull private final GroupCallPeekJobData groupCallPeekJobData;

  public static void enqueue(@NonNull GroupCallPeekJobData groupCallPeekJobData) {
    JobManager         jobManager = AppDependencies.getJobManager();
    String             queue      = QUEUE + groupCallPeekJobData.groupRecipientId;
    Parameters.Builder parameters = new Parameters.Builder()
                                                  .setQueue(queue)
                                                  .addConstraint(DecryptionsDrainedConstraint.KEY);

    jobManager.cancelAllInQueue(queue);

    jobManager.add(new GroupCallPeekJob(parameters.build(), groupCallPeekJobData));
  }

  private GroupCallPeekJob(@NonNull Parameters parameters,
                           @NonNull GroupCallPeekJobData groupCallPeekJobData)
  {
    super(parameters);
    this.groupCallPeekJobData = groupCallPeekJobData;
  }

  @Override
  protected void onRun() {
    AppDependencies.getJobManager().add(new GroupCallPeekWorkerJob(groupCallPeekJobData));
  }

  @Override
  protected boolean onShouldRetry(@NonNull Exception e) {
    return false;
  }

  @Override
  public @NonNull byte[] serialize() {
    return groupCallPeekJobData.encode();
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  @Override
  public void onFailure() {
  }

  public static final class Factory implements Job.Factory<GroupCallPeekJob> {

    @Override
    public @NonNull GroupCallPeekJob create(@NonNull Parameters parameters, @Nullable byte[] serializedData) {
      try {
        GroupCallPeekJobData jobData = GroupCallPeekJobData.ADAPTER.decode(serializedData);
        return new GroupCallPeekJob(parameters, jobData);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
