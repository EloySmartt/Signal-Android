package com.smarttmessenger.app.jobmanager.migrations;

import androidx.annotation.NonNull;

import com.smarttmessenger.app.jobmanager.JobMigration;

/**
 * We removed the messageId property from the job data and replaced it with a serialized envelope,
 * so we need to take jobs that referenced an ID and replace it with the envelope instead.
 *
 * @deprecated No longer have a PushDecryptJob to migrate, job now maps to {@link com.smarttmessenger.app.jobs.FailingJob}
 * in {@link com.smarttmessenger.app.jobs.JobManagerFactories}
 */
public class PushDecryptMessageJobEnvelopeMigration extends JobMigration {

  public PushDecryptMessageJobEnvelopeMigration() {
    super(8);
  }

  @Override
  public @NonNull JobData migrate(@NonNull JobData jobData) {
    return jobData;
  }
}
