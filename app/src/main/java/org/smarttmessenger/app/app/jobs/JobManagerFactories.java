package com.smarttmessenger.app.jobs;

import android.app.Application;

import androidx.annotation.NonNull;

import com.smarttmessenger.app.database.SignalDatabase;
import com.smarttmessenger.app.jobmanager.Constraint;
import com.smarttmessenger.app.jobmanager.ConstraintObserver;
import com.smarttmessenger.app.jobmanager.Job;
import com.smarttmessenger.app.jobmanager.JobMigration;
import com.smarttmessenger.app.jobmanager.impl.AutoDownloadEmojiConstraint;
import com.smarttmessenger.app.jobmanager.impl.CellServiceConstraintObserver;
import com.smarttmessenger.app.jobmanager.impl.ChangeNumberConstraint;
import com.smarttmessenger.app.jobmanager.impl.ChangeNumberConstraintObserver;
import com.smarttmessenger.app.jobmanager.impl.ChargingConstraint;
import com.smarttmessenger.app.jobmanager.impl.ChargingConstraintObserver;
import com.smarttmessenger.app.jobmanager.impl.DataRestoreConstraint;
import com.smarttmessenger.app.jobmanager.impl.DataRestoreConstraintObserver;
import com.smarttmessenger.app.jobmanager.impl.DecryptionsDrainedConstraint;
import com.smarttmessenger.app.jobmanager.impl.DecryptionsDrainedConstraintObserver;
import com.smarttmessenger.app.jobmanager.impl.NetworkConstraint;
import com.smarttmessenger.app.jobmanager.impl.NetworkConstraintObserver;
import com.smarttmessenger.app.jobmanager.impl.NetworkOrCellServiceConstraint;
import com.smarttmessenger.app.jobmanager.impl.NotInCallConstraint;
import com.smarttmessenger.app.jobmanager.impl.NotInCallConstraintObserver;
import com.smarttmessenger.app.jobmanager.impl.SqlCipherMigrationConstraint;
import com.smarttmessenger.app.jobmanager.impl.SqlCipherMigrationConstraintObserver;
import com.smarttmessenger.app.jobmanager.impl.WifiConstraint;
import com.smarttmessenger.app.jobmanager.migrations.DonationReceiptRedemptionJobMigration;
import com.smarttmessenger.app.jobmanager.migrations.GroupCallPeekJobDataMigration;
import com.smarttmessenger.app.jobmanager.migrations.PushDecryptMessageJobEnvelopeMigration;
import com.smarttmessenger.app.jobmanager.migrations.PushProcessMessageJobMigration;
import com.smarttmessenger.app.jobmanager.migrations.PushProcessMessageQueueJobMigration;
import com.smarttmessenger.app.jobmanager.migrations.RecipientIdFollowUpJobMigration;
import com.smarttmessenger.app.jobmanager.migrations.RecipientIdFollowUpJobMigration2;
import com.smarttmessenger.app.jobmanager.migrations.RecipientIdJobMigration;
import com.smarttmessenger.app.jobmanager.migrations.RetrieveProfileJobMigration;
import com.smarttmessenger.app.jobmanager.migrations.SendReadReceiptsJobMigration;
import com.smarttmessenger.app.jobmanager.migrations.SenderKeyDistributionSendJobRecipientMigration;
import com.smarttmessenger.app.migrations.AccountConsistencyMigrationJob;
import com.smarttmessenger.app.migrations.AccountRecordMigrationJob;
import com.smarttmessenger.app.migrations.ApplyUnknownFieldsToSelfMigrationJob;
import com.smarttmessenger.app.migrations.AttachmentCleanupMigrationJob;
import com.smarttmessenger.app.migrations.AttachmentHashBackfillMigrationJob;
import com.smarttmessenger.app.migrations.AttributesMigrationJob;
import com.smarttmessenger.app.migrations.AvatarIdRemovalMigrationJob;
import com.smarttmessenger.app.migrations.AvatarMigrationJob;
import com.smarttmessenger.app.migrations.BackfillDigestsMigrationJob;
import com.smarttmessenger.app.migrations.BackupJitterMigrationJob;
import com.smarttmessenger.app.migrations.BackupNotificationMigrationJob;
import com.smarttmessenger.app.migrations.BlobStorageLocationMigrationJob;
import com.smarttmessenger.app.migrations.CachedAttachmentsMigrationJob;
import com.smarttmessenger.app.migrations.ClearGlideCacheMigrationJob;
import com.smarttmessenger.app.migrations.ContactLinkRebuildMigrationJob;
import com.smarttmessenger.app.migrations.CopyUsernameToSignalStoreMigrationJob;
import com.smarttmessenger.app.migrations.DatabaseMigrationJob;
import com.smarttmessenger.app.migrations.DeleteDeprecatedLogsMigrationJob;
import com.smarttmessenger.app.migrations.DirectoryRefreshMigrationJob;
import com.smarttmessenger.app.migrations.EmojiDownloadMigrationJob;
import com.smarttmessenger.app.migrations.EmojiSearchIndexCheckMigrationJob;
import com.smarttmessenger.app.migrations.IdentityTableCleanupMigrationJob;
import com.smarttmessenger.app.migrations.LegacyMigrationJob;
import com.smarttmessenger.app.migrations.MigrationCompleteJob;
import com.smarttmessenger.app.migrations.OptimizeMessageSearchIndexMigrationJob;
import com.smarttmessenger.app.migrations.PassingMigrationJob;
import com.smarttmessenger.app.migrations.PinOptOutMigration;
import com.smarttmessenger.app.migrations.PinReminderMigrationJob;
import com.smarttmessenger.app.migrations.PniAccountInitializationMigrationJob;
import com.smarttmessenger.app.migrations.PniMigrationJob;
import com.smarttmessenger.app.migrations.PnpLaunchMigrationJob;
import com.smarttmessenger.app.migrations.PreKeysSyncMigrationJob;
import com.smarttmessenger.app.migrations.ProfileMigrationJob;
import com.smarttmessenger.app.migrations.ProfileSharingUpdateMigrationJob;
import com.smarttmessenger.app.migrations.RebuildMessageSearchIndexMigrationJob;
import com.smarttmessenger.app.migrations.RecheckPaymentsMigrationJob;
import com.smarttmessenger.app.migrations.RecipientSearchMigrationJob;
import com.smarttmessenger.app.migrations.SelfRegisteredStateMigrationJob;
import com.smarttmessenger.app.migrations.StickerAdditionMigrationJob;
import com.smarttmessenger.app.migrations.StickerDayByDayMigrationJob;
import com.smarttmessenger.app.migrations.StickerLaunchMigrationJob;
import com.smarttmessenger.app.migrations.StickerMyDailyLifeMigrationJob;
import com.smarttmessenger.app.migrations.StorageCapabilityMigrationJob;
import com.smarttmessenger.app.migrations.StorageFixLocalUnknownMigrationJob;
import com.smarttmessenger.app.migrations.StorageServiceMigrationJob;
import com.smarttmessenger.app.migrations.StorageServiceSystemNameMigrationJob;
import com.smarttmessenger.app.migrations.StoryViewedReceiptsStateMigrationJob;
import com.smarttmessenger.app.migrations.SubscriberIdMigrationJob;
import com.smarttmessenger.app.migrations.Svr2MirrorMigrationJob;
import com.smarttmessenger.app.migrations.SyncCallLinksMigrationJob;
import com.smarttmessenger.app.migrations.SyncDistributionListsMigrationJob;
import com.smarttmessenger.app.migrations.SyncKeysMigrationJob;
import com.smarttmessenger.app.migrations.TrimByLengthSettingsMigrationJob;
import com.smarttmessenger.app.migrations.UpdateSmsJobsMigrationJob;
import com.smarttmessenger.app.migrations.UserNotificationMigrationJob;
import com.smarttmessenger.app.migrations.UuidMigrationJob;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JobManagerFactories {

  public static Map<String, Job.Factory> getJobFactories(@NonNull Application application) {
    return new HashMap<String, Job.Factory>() {{
      put(AccountConsistencyWorkerJob.KEY,           new AccountConsistencyWorkerJob.Factory());
      put(AnalyzeDatabaseJob.KEY,                    new AnalyzeDatabaseJob.Factory());
      put(ApkUpdateJob.KEY,                          new ApkUpdateJob.Factory());
      put(ArchiveAttachmentBackfillJob.KEY,          new ArchiveAttachmentBackfillJob.Factory());
      put(ArchiveThumbnailUploadJob.KEY,             new ArchiveThumbnailUploadJob.Factory());
      put(AttachmentCompressionJob.KEY,              new AttachmentCompressionJob.Factory());
      put(AttachmentCopyJob.KEY,                     new AttachmentCopyJob.Factory());
      put(AttachmentDownloadJob.KEY,                 new AttachmentDownloadJob.Factory());
      put(AttachmentHashBackfillJob.KEY,             new AttachmentHashBackfillJob.Factory());
      put(AttachmentMarkUploadedJob.KEY,             new AttachmentMarkUploadedJob.Factory());
      put(AttachmentUploadJob.KEY,                   new AttachmentUploadJob.Factory());
      put(AutomaticSessionResetJob.KEY,              new AutomaticSessionResetJob.Factory());
      put(AvatarGroupsV1DownloadJob.KEY,             new AvatarGroupsV1DownloadJob.Factory());
      put(AvatarGroupsV2DownloadJob.KEY,             new AvatarGroupsV2DownloadJob.Factory());
      put(BackfillDigestJob.KEY,                     new BackfillDigestJob.Factory());
      put(BackupMessagesJob.KEY,                     new BackupMessagesJob.Factory());
      put(BackupRestoreJob.KEY,                      new BackupRestoreJob.Factory());
      put(BackupRestoreMediaJob.KEY,                 new BackupRestoreMediaJob.Factory());
      put(BoostReceiptRequestResponseJob.KEY,        new BoostReceiptRequestResponseJob.Factory());
      put(BuildExpirationConfirmationJob.KEY,        new BuildExpirationConfirmationJob.Factory());
      put(CallLinkPeekJob.KEY,                       new CallLinkPeekJob.Factory());
      put(CallLinkUpdateSendJob.KEY,                 new CallLinkUpdateSendJob.Factory());
      put(CallLogEventSendJob.KEY,                   new CallLogEventSendJob.Factory());
      put(CallSyncEventJob.KEY,                      new CallSyncEventJob.Factory());
      put(CheckRestoreMediaLeftJob.KEY,              new CheckRestoreMediaLeftJob.Factory());
      put(CheckServiceReachabilityJob.KEY,           new CheckServiceReachabilityJob.Factory());
      put(CleanPreKeysJob.KEY,                       new CleanPreKeysJob.Factory());
      put(ContactLinkRebuildMigrationJob.KEY,        new ContactLinkRebuildMigrationJob.Factory());
      put(ConversationShortcutRankingUpdateJob.KEY,  new ConversationShortcutRankingUpdateJob.Factory());
      put(ConversationShortcutUpdateJob.KEY,         new ConversationShortcutUpdateJob.Factory());
      put(CopyAttachmentToArchiveJob.KEY,            new CopyAttachmentToArchiveJob.Factory());
      put(CreateReleaseChannelJob.KEY,               new CreateReleaseChannelJob.Factory());
      put(DirectoryRefreshJob.KEY,                   new DirectoryRefreshJob.Factory());
      put(DonationReceiptRedemptionJob.KEY,          new DonationReceiptRedemptionJob.Factory());
      put(DownloadLatestEmojiDataJob.KEY,            new DownloadLatestEmojiDataJob.Factory());
      put(EmojiSearchIndexDownloadJob.KEY,           new EmojiSearchIndexDownloadJob.Factory());
      put(FcmRefreshJob.KEY,                         new FcmRefreshJob.Factory());
      put(FetchRemoteMegaphoneImageJob.KEY,          new FetchRemoteMegaphoneImageJob.Factory());
      put(FontDownloaderJob.KEY,                     new FontDownloaderJob.Factory());
      put(ForceUpdateGroupV2Job.KEY,                 new ForceUpdateGroupV2Job.Factory());
      put(ForceUpdateGroupV2WorkerJob.KEY,           new ForceUpdateGroupV2WorkerJob.Factory());
      put(GenerateAudioWaveFormJob.KEY,              new GenerateAudioWaveFormJob.Factory());
      put(GiftSendJob.KEY,                           new GiftSendJob.Factory());
      put(GroupCallUpdateSendJob.KEY,                new GroupCallUpdateSendJob.Factory());
      put(GroupCallPeekJob.KEY,                      new GroupCallPeekJob.Factory());
      put(GroupCallPeekWorkerJob.KEY,                new GroupCallPeekWorkerJob.Factory());
      put(GroupRingCleanupJob.KEY,                   new GroupRingCleanupJob.Factory());
      put(GroupV2UpdateSelfProfileKeyJob.KEY,        new GroupV2UpdateSelfProfileKeyJob.Factory());
      put(InAppPaymentAuthCheckJob.KEY,              new InAppPaymentAuthCheckJob.Factory());
      put(InAppPaymentGiftSendJob.KEY,               new InAppPaymentGiftSendJob.Factory());
      put(InAppPaymentKeepAliveJob.KEY,              new InAppPaymentKeepAliveJob.Factory());
      put(InAppPaymentRecurringContextJob.KEY,       new InAppPaymentRecurringContextJob.Factory());
      put(InAppPaymentOneTimeContextJob.KEY,         new InAppPaymentOneTimeContextJob.Factory());
      put(InAppPaymentRedemptionJob.KEY,             new InAppPaymentRedemptionJob.Factory());
      put(IndividualSendJob.KEY,                     new IndividualSendJob.Factory());
      put(LeaveGroupV2Job.KEY,                       new LeaveGroupV2Job.Factory());
      put(LeaveGroupV2WorkerJob.KEY,                 new LeaveGroupV2WorkerJob.Factory());
      put(LinkedDeviceInactiveCheckJob.KEY,          new LinkedDeviceInactiveCheckJob.Factory());
      put(LocalArchiveJob.KEY,                       new LocalArchiveJob.Factory());
      put(LocalBackupJob.KEY,                        new LocalBackupJob.Factory());
      put(LocalBackupJobApi29.KEY,                   new LocalBackupJobApi29.Factory());
      put(MarkerJob.KEY,                             new MarkerJob.Factory());
      put(MultiDeviceBlockedUpdateJob.KEY,           new MultiDeviceBlockedUpdateJob.Factory());
      put(MultiDeviceCallLinkSyncJob.KEY,            new MultiDeviceCallLinkSyncJob.Factory());
      put(MultiDeviceConfigurationUpdateJob.KEY,     new MultiDeviceConfigurationUpdateJob.Factory());
      put(MultiDeviceContactSyncJob.KEY,             new MultiDeviceContactSyncJob.Factory());
      put(MultiDeviceContactUpdateJob.KEY,           new MultiDeviceContactUpdateJob.Factory());
      put(MultiDeviceDeleteSyncJob.KEY,              new MultiDeviceDeleteSyncJob.Factory());
      put(MultiDeviceKeysUpdateJob.KEY,              new MultiDeviceKeysUpdateJob.Factory());
      put(MultiDeviceMessageRequestResponseJob.KEY,  new MultiDeviceMessageRequestResponseJob.Factory());
      put(MultiDeviceOutgoingPaymentSyncJob.KEY,     new MultiDeviceOutgoingPaymentSyncJob.Factory());
      put(MultiDeviceProfileContentUpdateJob.KEY,    new MultiDeviceProfileContentUpdateJob.Factory());
      put(MultiDeviceProfileKeyUpdateJob.KEY,        new MultiDeviceProfileKeyUpdateJob.Factory());
      put(MultiDeviceReadUpdateJob.KEY,              new MultiDeviceReadUpdateJob.Factory());
      put(MultiDeviceStickerPackOperationJob.KEY,    new MultiDeviceStickerPackOperationJob.Factory());
      put(MultiDeviceStickerPackSyncJob.KEY,         new MultiDeviceStickerPackSyncJob.Factory());
      put(MultiDeviceStorageSyncRequestJob.KEY,      new MultiDeviceStorageSyncRequestJob.Factory());
      put(MultiDeviceSubscriptionSyncRequestJob.KEY, new MultiDeviceSubscriptionSyncRequestJob.Factory());
      put(MultiDeviceVerifiedUpdateJob.KEY,          new MultiDeviceVerifiedUpdateJob.Factory());
      put(MultiDeviceViewOnceOpenJob.KEY,            new MultiDeviceViewOnceOpenJob.Factory());
      put(MultiDeviceViewedUpdateJob.KEY,            new MultiDeviceViewedUpdateJob.Factory());
      put(NullMessageSendJob.KEY,                    new NullMessageSendJob.Factory());
      put(OptimizeMessageSearchIndexJob.KEY,         new OptimizeMessageSearchIndexJob.Factory());
      put(PaymentLedgerUpdateJob.KEY,                new PaymentLedgerUpdateJob.Factory());
      put(PaymentNotificationSendJob.KEY,            new PaymentNotificationSendJob.Factory());
      put(PaymentNotificationSendJobV2.KEY,          new PaymentNotificationSendJobV2.Factory());
      put(PaymentSendJob.KEY,                        new PaymentSendJob.Factory());
      put(PaymentTransactionCheckJob.KEY,            new PaymentTransactionCheckJob.Factory());
      put(PnpInitializeDevicesJob.KEY,               new PnpInitializeDevicesJob.Factory());
      put(PreKeysSyncJob.KEY,                        new PreKeysSyncJob.Factory());
      put(ExternalLaunchDonationJob.KEY,             new ExternalLaunchDonationJob.Factory());
      put(ProfileKeySendJob.KEY,                     new ProfileKeySendJob.Factory());
      put(ProfileUploadJob.KEY,                      new ProfileUploadJob.Factory());
      put(PushDistributionListSendJob.KEY,           new PushDistributionListSendJob.Factory());
      put(PushGroupSendJob.KEY,                      new PushGroupSendJob.Factory());
      put(PushGroupSilentUpdateSendJob.KEY,          new PushGroupSilentUpdateSendJob.Factory());
      put(MessageFetchJob.KEY,                       new MessageFetchJob.Factory());
      put(PushProcessEarlyMessagesJob.KEY,           new PushProcessEarlyMessagesJob.Factory());
      put(PushProcessMessageErrorJob.KEY,            new PushProcessMessageErrorJob.Factory());
      put(PushProcessMessageJob.KEY,                 new PushProcessMessageJob.Factory());
      put(ReactionSendJob.KEY,                       new ReactionSendJob.Factory());
      put(RebuildMessageSearchIndexJob.KEY,          new RebuildMessageSearchIndexJob.Factory());
      put(ReclaimUsernameAndLinkJob.KEY,             new ReclaimUsernameAndLinkJob.Factory());
      put(RefreshAttributesJob.KEY,                  new RefreshAttributesJob.Factory());
      put(RefreshCallLinkDetailsJob.KEY,             new RefreshCallLinkDetailsJob.Factory());
      put(RefreshSvrCredentialsJob.KEY,              new RefreshSvrCredentialsJob.Factory());
      put(RefreshOwnProfileJob.KEY,                  new RefreshOwnProfileJob.Factory());
      put(RemoteConfigRefreshJob.KEY,                new RemoteConfigRefreshJob.Factory());
      put(RemoteDeleteSendJob.KEY,                   new RemoteDeleteSendJob.Factory());
      put(ReportSpamJob.KEY,                         new ReportSpamJob.Factory());
      put(ResendMessageJob.KEY,                      new ResendMessageJob.Factory());
      put(ResumableUploadSpecJob.KEY,                new ResumableUploadSpecJob.Factory());
      put(RequestGroupV2InfoWorkerJob.KEY,           new RequestGroupV2InfoWorkerJob.Factory());
      put(RequestGroupV2InfoJob.KEY,                 new RequestGroupV2InfoJob.Factory());
      put(RestoreAttachmentJob.KEY,                  new RestoreAttachmentJob.Factory());
      put(RestoreAttachmentThumbnailJob.KEY,         new RestoreAttachmentThumbnailJob.Factory());
      put(RestoreLocalAttachmentJob.KEY,             new RestoreLocalAttachmentJob.Factory());
      put(RetrieveProfileAvatarJob.KEY,              new RetrieveProfileAvatarJob.Factory());
      put(RetrieveProfileJob.KEY,                    new RetrieveProfileJob.Factory());
      put(RetrieveRemoteAnnouncementsJob.KEY,        new RetrieveRemoteAnnouncementsJob.Factory());
      put(RotateCertificateJob.KEY,                  new RotateCertificateJob.Factory());
      put(RotateProfileKeyJob.KEY,                   new RotateProfileKeyJob.Factory());
      put(SenderKeyDistributionSendJob.KEY,          new SenderKeyDistributionSendJob.Factory());
      put(SendDeliveryReceiptJob.KEY,                new SendDeliveryReceiptJob.Factory());
      put(SendPaymentsActivatedJob.KEY,              new SendPaymentsActivatedJob.Factory());
      put(SendReadReceiptJob.KEY,                    new SendReadReceiptJob.Factory(application));
      put(SendRetryReceiptJob.KEY,                   new SendRetryReceiptJob.Factory());
      put(SendViewedReceiptJob.KEY,                  new SendViewedReceiptJob.Factory(application));
      put(SyncSystemContactLinksJob.KEY,             new SyncSystemContactLinksJob.Factory());
      put(MultiDeviceStorySendSyncJob.KEY,           new MultiDeviceStorySendSyncJob.Factory());
      put(ResetSvrGuessCountJob.KEY,                 new ResetSvrGuessCountJob.Factory());
      put(ServiceOutageDetectionJob.KEY,             new ServiceOutageDetectionJob.Factory());
      put(StickerDownloadJob.KEY,                    new StickerDownloadJob.Factory());
      put(StickerPackDownloadJob.KEY,                new StickerPackDownloadJob.Factory());
      put(StorageAccountRestoreJob.KEY,              new StorageAccountRestoreJob.Factory());
      put(StorageForcePushJob.KEY,                   new StorageForcePushJob.Factory());
      put(StorageSyncJob.KEY,                        new StorageSyncJob.Factory());
      put(SubscriptionKeepAliveJob.KEY,              new SubscriptionKeepAliveJob.Factory());
      put(SubscriptionReceiptRequestResponseJob.KEY, new SubscriptionReceiptRequestResponseJob.Factory());
      put(SubscriberIdMigrationJob.KEY,              new SubscriberIdMigrationJob.Factory());
      put(StoryOnboardingDownloadJob.KEY,            new StoryOnboardingDownloadJob.Factory());
      put(SubmitRateLimitPushChallengeJob.KEY,       new SubmitRateLimitPushChallengeJob.Factory());
      put(Svr2MirrorJob.KEY,                         new Svr2MirrorJob.Factory());
      put(Svr3MirrorJob.KEY,                         new Svr3MirrorJob.Factory());
      put(SyncArchivedMediaJob.KEY,                  new SyncArchivedMediaJob.Factory());
      put(ThreadUpdateJob.KEY,                       new ThreadUpdateJob.Factory());
      put(TrimThreadJob.KEY,                         new TrimThreadJob.Factory());
      put(TypingSendJob.KEY,                         new TypingSendJob.Factory());
      put(UploadAttachmentToArchiveJob.KEY,          new UploadAttachmentToArchiveJob.Factory());

      // Migrations
      put(AccountConsistencyMigrationJob.KEY,        new AccountConsistencyMigrationJob.Factory());
      put(AccountRecordMigrationJob.KEY,             new AccountRecordMigrationJob.Factory());
      put(ApplyUnknownFieldsToSelfMigrationJob.KEY,  new ApplyUnknownFieldsToSelfMigrationJob.Factory());
      put(AttachmentCleanupMigrationJob.KEY,         new AttachmentCleanupMigrationJob.Factory());
      put(AttachmentHashBackfillMigrationJob.KEY,    new AttachmentHashBackfillMigrationJob.Factory());
      put(AttributesMigrationJob.KEY,                new AttributesMigrationJob.Factory());
      put(AvatarIdRemovalMigrationJob.KEY,           new AvatarIdRemovalMigrationJob.Factory());
      put(AvatarMigrationJob.KEY,                    new AvatarMigrationJob.Factory());
      put(BackfillDigestsMigrationJob.KEY,           new BackfillDigestsMigrationJob.Factory());
      put(BackupJitterMigrationJob.KEY,              new BackupJitterMigrationJob.Factory());
      put(BackupNotificationMigrationJob.KEY,        new BackupNotificationMigrationJob.Factory());
      put(BlobStorageLocationMigrationJob.KEY,       new BlobStorageLocationMigrationJob.Factory());
      put(CachedAttachmentsMigrationJob.KEY,         new CachedAttachmentsMigrationJob.Factory());
      put(ClearGlideCacheMigrationJob.KEY,           new ClearGlideCacheMigrationJob.Factory());
      put(CopyUsernameToSignalStoreMigrationJob.KEY, new CopyUsernameToSignalStoreMigrationJob.Factory());
      put(DatabaseMigrationJob.KEY,                  new DatabaseMigrationJob.Factory());
      put(DeleteDeprecatedLogsMigrationJob.KEY,      new DeleteDeprecatedLogsMigrationJob.Factory());
      put(DirectoryRefreshMigrationJob.KEY,          new DirectoryRefreshMigrationJob.Factory());
      put(EmojiDownloadMigrationJob.KEY,             new EmojiDownloadMigrationJob.Factory());
      put(EmojiSearchIndexCheckMigrationJob.KEY,     new EmojiSearchIndexCheckMigrationJob.Factory());
      put(IdentityTableCleanupMigrationJob.KEY,      new IdentityTableCleanupMigrationJob.Factory());
      put(LegacyMigrationJob.KEY,                    new LegacyMigrationJob.Factory());
      put(MigrationCompleteJob.KEY,                  new MigrationCompleteJob.Factory());
      put(OptimizeMessageSearchIndexMigrationJob.KEY,new OptimizeMessageSearchIndexMigrationJob.Factory());
      put(PinOptOutMigration.KEY,                    new PinOptOutMigration.Factory());
      put(PinReminderMigrationJob.KEY,               new PinReminderMigrationJob.Factory());
      put(PniAccountInitializationMigrationJob.KEY,  new PniAccountInitializationMigrationJob.Factory());
      put(PniMigrationJob.KEY,                       new PniMigrationJob.Factory());
      put(PnpLaunchMigrationJob.KEY,                 new PnpLaunchMigrationJob.Factory());
      put(PreKeysSyncMigrationJob.KEY,               new PreKeysSyncMigrationJob.Factory());
      put(ProfileMigrationJob.KEY,                   new ProfileMigrationJob.Factory());
      put(ProfileSharingUpdateMigrationJob.KEY,      new ProfileSharingUpdateMigrationJob.Factory());
      put(RebuildMessageSearchIndexMigrationJob.KEY, new RebuildMessageSearchIndexMigrationJob.Factory());
      put(RecheckPaymentsMigrationJob.KEY,           new RecheckPaymentsMigrationJob.Factory());
      put(RecipientSearchMigrationJob.KEY,           new RecipientSearchMigrationJob.Factory());
      put(SelfRegisteredStateMigrationJob.KEY,       new SelfRegisteredStateMigrationJob.Factory());
      put(StickerLaunchMigrationJob.KEY,             new StickerLaunchMigrationJob.Factory());
      put(StickerAdditionMigrationJob.KEY,           new StickerAdditionMigrationJob.Factory());
      put(StickerDayByDayMigrationJob.KEY,           new StickerDayByDayMigrationJob.Factory());
      put(StickerMyDailyLifeMigrationJob.KEY,        new StickerMyDailyLifeMigrationJob.Factory());
      put(StorageCapabilityMigrationJob.KEY,         new StorageCapabilityMigrationJob.Factory());
      put(StorageFixLocalUnknownMigrationJob.KEY,    new StorageFixLocalUnknownMigrationJob.Factory());
      put(StorageServiceMigrationJob.KEY,            new StorageServiceMigrationJob.Factory());
      put(StorageServiceSystemNameMigrationJob.KEY,  new StorageServiceSystemNameMigrationJob.Factory());
      put(StoryViewedReceiptsStateMigrationJob.KEY,  new StoryViewedReceiptsStateMigrationJob.Factory());
      put(Svr2MirrorMigrationJob.KEY,                new Svr2MirrorMigrationJob.Factory());
      put(SyncCallLinksMigrationJob.KEY,             new SyncCallLinksMigrationJob.Factory());
      put(SyncDistributionListsMigrationJob.KEY,     new SyncDistributionListsMigrationJob.Factory());
      put(SyncKeysMigrationJob.KEY,                  new SyncKeysMigrationJob.Factory());
      put(TrimByLengthSettingsMigrationJob.KEY,      new TrimByLengthSettingsMigrationJob.Factory());
      put(UpdateSmsJobsMigrationJob.KEY,             new UpdateSmsJobsMigrationJob.Factory());
      put(UserNotificationMigrationJob.KEY,          new UserNotificationMigrationJob.Factory());
      put(UuidMigrationJob.KEY,                      new UuidMigrationJob.Factory());

      // Dead jobs
      put(FailingJob.KEY,                            new FailingJob.Factory());
      put(PassingMigrationJob.KEY,                   new PassingMigrationJob.Factory());
      put("PushContentReceiveJob",                   new FailingJob.Factory());
      put("AttachmentUploadJob",                     new FailingJob.Factory());
      put("MmsSendJob",                              new FailingJob.Factory());
      put("RefreshUnidentifiedDeliveryAbilityJob",   new FailingJob.Factory());
      put("Argon2TestJob",                           new FailingJob.Factory());
      put("Argon2TestMigrationJob",                  new PassingMigrationJob.Factory());
      put("StorageKeyRotationMigrationJob",          new PassingMigrationJob.Factory());
      put("StorageSyncJob",                          new StorageSyncJob.Factory());
      put("WakeGroupV2Job",                          new FailingJob.Factory());
      put("LeaveGroupJob",                           new FailingJob.Factory());
      put("PushGroupUpdateJob",                      new FailingJob.Factory());
      put("RequestGroupInfoJob",                     new FailingJob.Factory());
      put("RotateSignedPreKeyJob",                   new PreKeysSyncJob.Factory());
      put("CreateSignedPreKeyJob",                   new PreKeysSyncJob.Factory());
      put("RefreshPreKeysJob",                       new PreKeysSyncJob.Factory());
      put("RecipientChangedNumberJob",               new FailingJob.Factory());
      put("PushTextSendJob",                         new IndividualSendJob.Factory());
      put("MultiDevicePniIdentityUpdateJob",         new FailingJob.Factory());
      put("MultiDeviceGroupUpdateJob",               new FailingJob.Factory());
      put("CallSyncEventJob",                        new FailingJob.Factory());
      put("RegistrationPinV2MigrationJob",           new FailingJob.Factory());
      put("KbsEnclaveMigrationWorkerJob",            new FailingJob.Factory());
      put("KbsEnclaveMigrationJob",                  new PassingMigrationJob.Factory());
      put("ClearFallbackKbsEnclaveJob",              new FailingJob.Factory());
      put("PushDecryptJob",                          new FailingJob.Factory());
      put("PushDecryptDrainedJob",                   new FailingJob.Factory());
      put("PushProcessJob",                          new FailingJob.Factory());
      put("DecryptionsDrainedMigrationJob",          new PassingMigrationJob.Factory());
      put("MmsReceiveJob",                           new FailingJob.Factory());
      put("MmsDownloadJob",                          new FailingJob.Factory());
      put("SmsReceiveJob",                           new FailingJob.Factory());
      put("StoryReadStateMigrationJob",              new PassingMigrationJob.Factory());
      put("GroupV1MigrationJob",                     new FailingJob.Factory());
      put("NewRegistrationUsernameSyncJob",          new FailingJob.Factory());
      put("SmsSendJob",                              new FailingJob.Factory());
      put("SmsSentJob",                              new FailingJob.Factory());
      put("MmsSendJobV2",                            new FailingJob.Factory());
      put("AttachmentUploadJobV2",                   new FailingJob.Factory());
    }};
  }

  public static Map<String, Constraint.Factory> getConstraintFactories(@NonNull Application application) {
    return new HashMap<String, Constraint.Factory>() {{
      put(AutoDownloadEmojiConstraint.KEY,           new AutoDownloadEmojiConstraint.Factory(application));
      put(ChangeNumberConstraint.KEY,                new ChangeNumberConstraint.Factory());
      put(ChargingConstraint.KEY,                    new ChargingConstraint.Factory());
      put(DataRestoreConstraint.KEY,                 new DataRestoreConstraint.Factory());
      put(DecryptionsDrainedConstraint.KEY,          new DecryptionsDrainedConstraint.Factory());
      put(NetworkConstraint.KEY,                     new NetworkConstraint.Factory(application));
      put(NetworkOrCellServiceConstraint.KEY,        new NetworkOrCellServiceConstraint.Factory(application));
      put(NetworkOrCellServiceConstraint.LEGACY_KEY, new NetworkOrCellServiceConstraint.Factory(application));
      put(NotInCallConstraint.KEY,                   new NotInCallConstraint.Factory());
      put(SqlCipherMigrationConstraint.KEY,          new SqlCipherMigrationConstraint.Factory(application));
      put(WifiConstraint.KEY,                        new WifiConstraint.Factory(application));
    }};
  }

  public static List<ConstraintObserver> getConstraintObservers(@NonNull Application application) {
    return Arrays.asList(CellServiceConstraintObserver.getInstance(application),
                         new ChargingConstraintObserver(application),
                         new NetworkConstraintObserver(application),
                         new SqlCipherMigrationConstraintObserver(),
                         new DecryptionsDrainedConstraintObserver(),
                         new NotInCallConstraintObserver(),
                         ChangeNumberConstraintObserver.INSTANCE,
                         DataRestoreConstraintObserver.INSTANCE);
  }

  public static List<JobMigration> getJobMigrations(@NonNull Application application) {
    return Arrays.asList(new RecipientIdJobMigration(application),
                         new RecipientIdFollowUpJobMigration(),
                         new RecipientIdFollowUpJobMigration2(),
                         new SendReadReceiptsJobMigration(SignalDatabase.messages()),
                         new PushProcessMessageQueueJobMigration(application),
                         new RetrieveProfileJobMigration(),
                         new PushDecryptMessageJobEnvelopeMigration(),
                         new SenderKeyDistributionSendJobRecipientMigration(),
                         new PushProcessMessageJobMigration(),
                         new DonationReceiptRedemptionJobMigration(),
                         new GroupCallPeekJobDataMigration());
  }
}
