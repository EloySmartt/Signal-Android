package com.smarttmessenger.app.jobs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.signal.core.util.logging.Log;
import com.smarttmessenger.app.database.SignalDatabase;
import com.smarttmessenger.app.database.model.GroupRecord;
import com.smarttmessenger.app.groups.GroupChangeBusyException;
import com.smarttmessenger.app.groups.GroupId;
import com.smarttmessenger.app.groups.GroupManager;
import com.smarttmessenger.app.groups.GroupNotAMemberException;
import com.smarttmessenger.app.groups.v2.processing.GroupsV2StateProcessor;
import com.smarttmessenger.app.jobmanager.JsonJobData;
import com.smarttmessenger.app.jobmanager.Job;
import com.smarttmessenger.app.jobmanager.impl.NetworkConstraint;
import com.smarttmessenger.app.recipients.Recipient;
import org.whispersystems.signalservice.api.groupsv2.NoCredentialForRedemptionTimeException;
import org.whispersystems.signalservice.api.push.exceptions.PushNetworkException;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Scheduled by {@link RequestGroupV2InfoJob} after message queues are drained.
 */
final class RequestGroupV2InfoWorkerJob extends BaseJob {

  public static final String KEY = "RequestGroupV2InfoWorkerJob";

  private static final String TAG = Log.tag(RequestGroupV2InfoWorkerJob.class);

  private static final String KEY_GROUP_ID    = "group_id";
  private static final String KEY_TO_REVISION = "to_revision";

  private final GroupId.V2 groupId;
  private final int        toRevision;

  @WorkerThread
  RequestGroupV2InfoWorkerJob(@NonNull GroupId.V2 groupId, int toRevision) {
    this(new Parameters.Builder()
                       .setQueue(PushProcessMessageJob.getQueueName(Recipient.externalGroupExact(groupId).getId()))
                       .addConstraint(NetworkConstraint.KEY)
                       .setLifespan(TimeUnit.DAYS.toMillis(1))
                       .setMaxAttempts(Parameters.UNLIMITED)
                       .build(),
         groupId,
         toRevision);
  }

  private RequestGroupV2InfoWorkerJob(@NonNull Parameters parameters, @NonNull GroupId.V2 groupId, int toRevision) {
    super(parameters);

    this.groupId    = groupId;
    this.toRevision = toRevision;
  }

  @Override
  public @Nullable byte[] serialize() {
    return new JsonJobData.Builder().putString(KEY_GROUP_ID, groupId.toString())
                                    .putInt(KEY_TO_REVISION, toRevision)
                                    .serialize();
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  @Override
  public void onRun() throws IOException, GroupNotAMemberException, GroupChangeBusyException {
    if (toRevision == GroupsV2StateProcessor.LATEST) {
      Log.i(TAG, "Updating group to latest revision");
    } else {
      Log.i(TAG, "Updating group to revision " + toRevision);
    }

    Optional<GroupRecord> group = SignalDatabase.groups().getGroup(groupId);

    if (!group.isPresent()) {
      Log.w(TAG, "Group not found");
      return;
    }

    if (Recipient.externalGroupExact(groupId).isBlocked()) {
      Log.i(TAG, "Not fetching group info for blocked group " + groupId);
      return;
    }

    GroupManager.updateGroupFromServer(context, group.get().requireV2GroupProperties().getGroupMasterKey(), toRevision, System.currentTimeMillis());
  }

  @Override
  public boolean onShouldRetry(@NonNull Exception e) {
    return e instanceof PushNetworkException ||
           e instanceof NoCredentialForRedemptionTimeException ||
           e instanceof GroupChangeBusyException;
  }

  @Override
  public void onFailure() {
  }

  public static final class Factory implements Job.Factory<RequestGroupV2InfoWorkerJob> {

    @Override
    public @NonNull RequestGroupV2InfoWorkerJob create(@NonNull Parameters parameters, @Nullable byte[] serializedData) {
      JsonJobData data = JsonJobData.deserialize(serializedData);

      return new RequestGroupV2InfoWorkerJob(parameters,
                                             GroupId.parseOrThrow(data.getString(KEY_GROUP_ID)).requireV2(),
                                             data.getInt(KEY_TO_REVISION));
    }
  }
}
