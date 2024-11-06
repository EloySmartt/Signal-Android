package com.smarttmessenger.app.dependencies;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import org.signal.billing.BillingFactory;
import org.signal.core.util.ThreadUtil;
import org.signal.core.util.billing.BillingApi;
import org.signal.core.util.concurrent.DeadlockDetector;
import org.signal.core.util.concurrent.SignalExecutors;
import org.signal.libsignal.net.Network;
import org.signal.libsignal.zkgroup.profiles.ClientZkProfileOperations;
import org.signal.libsignal.zkgroup.receipts.ClientZkReceiptOperations;
import com.smarttmessenger.app.BuildConfig;
import com.smarttmessenger.app.components.TypingStatusRepository;
import com.smarttmessenger.app.components.TypingStatusSender;
import com.smarttmessenger.app.crypto.ReentrantSessionLock;
import com.smarttmessenger.app.crypto.storage.SignalBaseIdentityKeyStore;
import com.smarttmessenger.app.crypto.storage.SignalIdentityKeyStore;
import com.smarttmessenger.app.crypto.storage.SignalKyberPreKeyStore;
import com.smarttmessenger.app.crypto.storage.SignalSenderKeyStore;
import com.smarttmessenger.app.crypto.storage.SignalServiceAccountDataStoreImpl;
import com.smarttmessenger.app.crypto.storage.SignalServiceDataStoreImpl;
import com.smarttmessenger.app.crypto.storage.TextSecurePreKeyStore;
import com.smarttmessenger.app.crypto.storage.TextSecureSessionStore;
import com.smarttmessenger.app.database.DatabaseObserver;
import com.smarttmessenger.app.database.JobDatabase;
import com.smarttmessenger.app.database.PendingRetryReceiptCache;
import com.smarttmessenger.app.jobmanager.JobManager;
import com.smarttmessenger.app.jobmanager.JobMigrator;
import com.smarttmessenger.app.jobmanager.impl.FactoryJobPredicate;
import com.smarttmessenger.app.jobs.FastJobStorage;
import com.smarttmessenger.app.jobs.GroupCallUpdateSendJob;
import com.smarttmessenger.app.jobs.IndividualSendJob;
import com.smarttmessenger.app.jobs.JobManagerFactories;
import com.smarttmessenger.app.jobs.MarkerJob;
import com.smarttmessenger.app.jobs.PreKeysSyncJob;
import com.smarttmessenger.app.jobs.PushGroupSendJob;
import com.smarttmessenger.app.jobs.PushProcessMessageJob;
import com.smarttmessenger.app.jobs.ReactionSendJob;
import com.smarttmessenger.app.jobs.TypingSendJob;
import com.smarttmessenger.app.keyvalue.SignalStore;
import com.smarttmessenger.app.megaphone.MegaphoneRepository;
import com.smarttmessenger.app.messages.IncomingMessageObserver;
import com.smarttmessenger.app.net.DefaultWebSocketShadowingBridge;
import com.smarttmessenger.app.net.SignalWebSocketHealthMonitor;
import com.smarttmessenger.app.net.StandardUserAgentInterceptor;
import com.smarttmessenger.app.notifications.MessageNotifier;
import com.smarttmessenger.app.notifications.OptimizedMessageNotifier;
import com.smarttmessenger.app.payments.MobileCoinConfig;
import com.smarttmessenger.app.payments.Payments;
import com.smarttmessenger.app.push.SecurityEventListener;
import com.smarttmessenger.app.push.SignalServiceNetworkAccess;
import com.smarttmessenger.app.recipients.LiveRecipientCache;
import com.smarttmessenger.app.revealable.ViewOnceMessageManager;
import com.smarttmessenger.app.service.DeletedCallEventManager;
import com.smarttmessenger.app.service.ExpiringMessageManager;
import com.smarttmessenger.app.service.ExpiringStoriesManager;
import com.smarttmessenger.app.service.PendingRetryReceiptManager;
import com.smarttmessenger.app.service.ScheduledMessageManager;
import com.smarttmessenger.app.service.TrimThreadsByDateManager;
import com.smarttmessenger.app.service.webrtc.SignalCallManager;
import com.smarttmessenger.app.shakereport.ShakeToReport;
import com.smarttmessenger.app.stories.Stories;
import com.smarttmessenger.app.util.AlarmSleepTimer;
import com.smarttmessenger.app.util.AppForegroundObserver;
import com.smarttmessenger.app.util.ByteUnit;
import com.smarttmessenger.app.util.EarlyMessageCache;
import com.smarttmessenger.app.util.FrameRateTracker;
import com.smarttmessenger.app.util.RemoteConfig;
import com.smarttmessenger.app.util.TextSecurePreferences;
import com.smarttmessenger.app.video.exo.GiphyMp4Cache;
import com.smarttmessenger.app.video.exo.SimpleExoPlayerPool;
import com.smarttmessenger.app.webrtc.audio.AudioManagerCompat;
import org.whispersystems.signalservice.api.SignalServiceAccountManager;
import org.whispersystems.signalservice.api.SignalServiceDataStore;
import org.whispersystems.signalservice.api.SignalServiceMessageReceiver;
import org.whispersystems.signalservice.api.SignalServiceMessageSender;
import org.whispersystems.signalservice.api.SignalWebSocket;
import org.whispersystems.signalservice.api.archive.ArchiveApi;
import org.whispersystems.signalservice.api.attachment.AttachmentApi;
import org.whispersystems.signalservice.api.groupsv2.ClientZkOperations;
import org.whispersystems.signalservice.api.groupsv2.GroupsV2Operations;
import org.whispersystems.signalservice.api.keys.KeysApi;
import org.whispersystems.signalservice.api.push.ServiceId.ACI;
import org.whispersystems.signalservice.api.push.ServiceId.PNI;
import org.whispersystems.signalservice.api.registration.RegistrationApi;
import org.whispersystems.signalservice.api.services.CallLinksService;
import org.whispersystems.signalservice.api.services.DonationsService;
import org.whispersystems.signalservice.api.services.ProfileService;
import org.whispersystems.signalservice.api.util.CredentialsProvider;
import org.whispersystems.signalservice.api.util.SleepTimer;
import org.whispersystems.signalservice.api.util.UptimeSleepTimer;
import org.whispersystems.signalservice.api.websocket.WebSocketFactory;
import org.whispersystems.signalservice.internal.configuration.SignalServiceConfiguration;
import org.whispersystems.signalservice.internal.push.PushServiceSocket;
import org.whispersystems.signalservice.internal.websocket.LibSignalChatConnection;
import org.whispersystems.signalservice.internal.websocket.LibSignalNetworkExtensions;
import org.whispersystems.signalservice.internal.websocket.OkHttpWebSocketConnection;
import org.whispersystems.signalservice.internal.websocket.ShadowingWebSocketConnection;
import org.whispersystems.signalservice.internal.websocket.WebSocketConnection;
import org.whispersystems.signalservice.internal.websocket.WebSocketShadowingBridge;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Implementation of {@link AppDependencies.Provider} that provides real app dependencies.
 */
public class ApplicationDependencyProvider implements AppDependencies.Provider {

  private final Application context;

  public ApplicationDependencyProvider(@NonNull Application context) {
    this.context = context;
  }

  private @NonNull ClientZkOperations provideClientZkOperations(@NonNull SignalServiceConfiguration signalServiceConfiguration) {
    return ClientZkOperations.create(signalServiceConfiguration);
  }

  @Override
  public @NonNull PushServiceSocket providePushServiceSocket(@NonNull SignalServiceConfiguration signalServiceConfiguration, @NonNull GroupsV2Operations groupsV2Operations) {
    return new PushServiceSocket(signalServiceConfiguration,
                                 new DynamicCredentialsProvider(),
                                 BuildConfig.SIGNAL_AGENT,
                                 groupsV2Operations.getProfileOperations(),
                                 RemoteConfig.okHttpAutomaticRetry());
  }

  @Override
  public @NonNull GroupsV2Operations provideGroupsV2Operations(@NonNull SignalServiceConfiguration signalServiceConfiguration) {
    return new GroupsV2Operations(provideClientZkOperations(signalServiceConfiguration), RemoteConfig.groupLimits().getHardLimit());
  }

  @Override
  public @NonNull SignalServiceAccountManager provideSignalServiceAccountManager(@NonNull PushServiceSocket pushServiceSocket, @NonNull GroupsV2Operations groupsV2Operations) {
    return new SignalServiceAccountManager(pushServiceSocket, groupsV2Operations);
  }

  @Override
  public @NonNull SignalServiceMessageSender provideSignalServiceMessageSender(@NonNull SignalWebSocket signalWebSocket, @NonNull SignalServiceDataStore protocolStore, @NonNull PushServiceSocket pushServiceSocket) {
      return new SignalServiceMessageSender(pushServiceSocket,
                                            protocolStore,
                                            ReentrantSessionLock.INSTANCE,
                                            signalWebSocket,
                                            Optional.of(new SecurityEventListener(context)),
                                            SignalExecutors.newCachedBoundedExecutor("signal-messages", ThreadUtil.PRIORITY_IMPORTANT_BACKGROUND_THREAD, 1, 16, 30),
                                            ByteUnit.KILOBYTES.toBytes(256));
  }

  @Override
  public @NonNull SignalServiceMessageReceiver provideSignalServiceMessageReceiver(@NonNull PushServiceSocket pushServiceSocket) {
    return new SignalServiceMessageReceiver(pushServiceSocket);
  }

  @Override
  public @NonNull SignalServiceNetworkAccess provideSignalServiceNetworkAccess() {
    return new SignalServiceNetworkAccess(context);
  }

  @Override
  public @NonNull LiveRecipientCache provideRecipientCache() {
    return new LiveRecipientCache(context);
  }

  @Override
  public @NonNull JobManager provideJobManager() {
    JobManager.Configuration config = new JobManager.Configuration.Builder()
                                                                  .setJobFactories(JobManagerFactories.getJobFactories(context))
                                                                  .setConstraintFactories(JobManagerFactories.getConstraintFactories(context))
                                                                  .setConstraintObservers(JobManagerFactories.getConstraintObservers(context))
                                                                  .setJobStorage(new FastJobStorage(JobDatabase.getInstance(context)))
                                                                  .setJobMigrator(new JobMigrator(TextSecurePreferences.getJobManagerVersion(context), JobManager.CURRENT_VERSION, JobManagerFactories.getJobMigrations(context)))
                                                                  .addReservedJobRunner(new FactoryJobPredicate(PushProcessMessageJob.KEY, MarkerJob.KEY))
                                                                  .addReservedJobRunner(new FactoryJobPredicate(IndividualSendJob.KEY, PushGroupSendJob.KEY, ReactionSendJob.KEY, TypingSendJob.KEY, GroupCallUpdateSendJob.KEY))
                                                                  .build();
    return new JobManager(context, config);
  }

  @Override
  public @NonNull FrameRateTracker provideFrameRateTracker() {
    return new FrameRateTracker(context);
  }

  @SuppressLint("DiscouragedApi")
  public @NonNull MegaphoneRepository provideMegaphoneRepository() {
    return new MegaphoneRepository(context);
  }

  @Override
  public @NonNull EarlyMessageCache provideEarlyMessageCache() {
    return new EarlyMessageCache();
  }

  @Override
  public @NonNull MessageNotifier provideMessageNotifier() {
    return new OptimizedMessageNotifier(context);
  }

  @Override
  public @NonNull IncomingMessageObserver provideIncomingMessageObserver() {
    return new IncomingMessageObserver(context);
  }

  @Override
  public @NonNull TrimThreadsByDateManager provideTrimThreadsByDateManager() {
    return new TrimThreadsByDateManager(context);
  }

  @Override
  public @NonNull ViewOnceMessageManager provideViewOnceMessageManager() {
    return new ViewOnceMessageManager(context);
  }

  @Override
  public @NonNull ExpiringStoriesManager provideExpiringStoriesManager() {
    return new ExpiringStoriesManager(context);
  }

  @Override
  public @NonNull ExpiringMessageManager provideExpiringMessageManager() {
    return new ExpiringMessageManager(context);
  }

  @Override
  public @NonNull DeletedCallEventManager provideDeletedCallEventManager() {
    return new DeletedCallEventManager(context);
  }

  @Override
  public @NonNull ScheduledMessageManager provideScheduledMessageManager() {
    return new ScheduledMessageManager(context);
  }

  @Override
  public @NonNull Network provideLibsignalNetwork(@NonNull SignalServiceConfiguration config) {
    Network network = new Network(BuildConfig.LIBSIGNAL_NET_ENV, StandardUserAgentInterceptor.USER_AGENT);
    LibSignalNetworkExtensions.applyConfiguration(network, config);
    return network;
  }

  @Override
  public @NonNull TypingStatusRepository provideTypingStatusRepository() {
    return new TypingStatusRepository();
  }

  @Override
  public @NonNull TypingStatusSender provideTypingStatusSender() {
    return new TypingStatusSender();
  }

  @Override
  public @NonNull DatabaseObserver provideDatabaseObserver() {
    return new DatabaseObserver();
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public @NonNull Payments providePayments(@NonNull SignalServiceAccountManager signalServiceAccountManager) {
    MobileCoinConfig network;

    if      (BuildConfig.MOBILE_COIN_ENVIRONMENT.equals("mainnet")) network = MobileCoinConfig.getMainNet(signalServiceAccountManager);
    else if (BuildConfig.MOBILE_COIN_ENVIRONMENT.equals("testnet")) network = MobileCoinConfig.getTestNet(signalServiceAccountManager);
    else throw new AssertionError("Unknown network " + BuildConfig.MOBILE_COIN_ENVIRONMENT);

    return new Payments(network);
  }

  @Override
  public @NonNull ShakeToReport provideShakeToReport() {
    return new ShakeToReport(context);
  }

  @Override
  public @NonNull SignalCallManager provideSignalCallManager() {
    return new SignalCallManager(context);
  }

  @Override
  public @NonNull PendingRetryReceiptManager providePendingRetryReceiptManager() {
    return new PendingRetryReceiptManager(context);
  }

  @Override
  public @NonNull PendingRetryReceiptCache providePendingRetryReceiptCache() {
    return new PendingRetryReceiptCache();
  }

  @Override
  public @NonNull SignalWebSocket provideSignalWebSocket(@NonNull Supplier<SignalServiceConfiguration> signalServiceConfigurationSupplier, @NonNull Supplier<Network> libSignalNetworkSupplier) {
    SleepTimer                   sleepTimer      = !SignalStore.account().isFcmEnabled() || SignalStore.internal().isWebsocketModeForced() ? new AlarmSleepTimer(context) : new UptimeSleepTimer();
    SignalWebSocketHealthMonitor healthMonitor   = new SignalWebSocketHealthMonitor(context, sleepTimer);
    WebSocketShadowingBridge     bridge          = new DefaultWebSocketShadowingBridge(context);
    SignalWebSocket              signalWebSocket = new SignalWebSocket(provideWebSocketFactory(signalServiceConfigurationSupplier, healthMonitor, libSignalNetworkSupplier, bridge));

    healthMonitor.monitor(signalWebSocket);

    return signalWebSocket;
  }

  @Override
  public @NonNull SignalServiceDataStoreImpl provideProtocolStore() {
    ACI localAci = SignalStore.account().getAci();
    PNI localPni = SignalStore.account().getPni();

    if (localAci == null) {
      throw new IllegalStateException("No ACI set!");
    }

    if (localPni == null) {
      throw new IllegalStateException("No PNI set!");
    }

    boolean needsPreKeyJob = false;

    if (!SignalStore.account().hasAciIdentityKey()) {
      SignalStore.account().generateAciIdentityKeyIfNecessary();
      needsPreKeyJob = true;
    }

    if (!SignalStore.account().hasPniIdentityKey()) {
      SignalStore.account().generatePniIdentityKeyIfNecessary();
      needsPreKeyJob = true;
    }

    if (needsPreKeyJob) {
      PreKeysSyncJob.enqueueIfNeeded();
    }

    SignalBaseIdentityKeyStore baseIdentityStore = new SignalBaseIdentityKeyStore(context);

    SignalServiceAccountDataStoreImpl aciStore = new SignalServiceAccountDataStoreImpl(context,
                                                                                       new TextSecurePreKeyStore(localAci),
                                                                                       new SignalKyberPreKeyStore(localAci),
                                                                                       new SignalIdentityKeyStore(baseIdentityStore, () -> SignalStore.account().getAciIdentityKey()),
                                                                                       new TextSecureSessionStore(localAci),
                                                                                       new SignalSenderKeyStore(context));

    SignalServiceAccountDataStoreImpl pniStore = new SignalServiceAccountDataStoreImpl(context,
                                                                                       new TextSecurePreKeyStore(localPni),
                                                                                       new SignalKyberPreKeyStore(localPni),
                                                                                       new SignalIdentityKeyStore(baseIdentityStore, () -> SignalStore.account().getPniIdentityKey()),
                                                                                       new TextSecureSessionStore(localPni),
                                                                                       new SignalSenderKeyStore(context));
    return new SignalServiceDataStoreImpl(context, aciStore, pniStore);
  }

  @Override
  public @NonNull GiphyMp4Cache provideGiphyMp4Cache() {
    return new GiphyMp4Cache(ByteUnit.MEGABYTES.toBytes(16));
  }

  @Override
  public @NonNull SimpleExoPlayerPool provideExoPlayerPool() {
    return new SimpleExoPlayerPool(context);
  }

  @Override
  public @NonNull AudioManagerCompat provideAndroidCallAudioManager() {
    return AudioManagerCompat.create(context);
  }

  @Override
  public @NonNull DonationsService provideDonationsService(@NonNull PushServiceSocket pushServiceSocket) {
    return new DonationsService(pushServiceSocket);
  }

  @Override
  public @NonNull CallLinksService provideCallLinksService(@NonNull PushServiceSocket pushServiceSocket) {
    return new CallLinksService(pushServiceSocket);
  }

  @Override
  public @NonNull ProfileService provideProfileService(@NonNull ClientZkProfileOperations clientZkProfileOperations,
                                                       @NonNull SignalServiceMessageReceiver receiver,
                                                       @NonNull SignalWebSocket signalWebSocket)
  {
    return new ProfileService(clientZkProfileOperations, receiver, signalWebSocket);
  }

  @Override
  public @NonNull DeadlockDetector provideDeadlockDetector() {
    HandlerThread handlerThread = new HandlerThread("signal-DeadlockDetector", ThreadUtil.PRIORITY_BACKGROUND_THREAD);
    handlerThread.start();
    return new DeadlockDetector(new Handler(handlerThread.getLooper()), TimeUnit.SECONDS.toMillis(5));
  }

  @Override
  public @NonNull ClientZkReceiptOperations provideClientZkReceiptOperations(@NonNull SignalServiceConfiguration signalServiceConfiguration) {
    return provideClientZkOperations(signalServiceConfiguration).getReceiptOperations();
  }

  @NonNull WebSocketFactory provideWebSocketFactory(@NonNull Supplier<SignalServiceConfiguration> signalServiceConfigurationSupplier,
                                                    @NonNull SignalWebSocketHealthMonitor healthMonitor,
                                                    @NonNull Supplier<Network> libSignalNetworkSupplier,
                                                    @NonNull WebSocketShadowingBridge bridge)
  {
    return new WebSocketFactory() {
      @Override
      public WebSocketConnection createWebSocket() {
        return new OkHttpWebSocketConnection("normal",
                                             signalServiceConfigurationSupplier.get(),
                                             Optional.of(new DynamicCredentialsProvider()),
                                             BuildConfig.SIGNAL_AGENT,
                                             healthMonitor,
                                             Stories.isFeatureEnabled());
      }

      @Override
      public WebSocketConnection createUnidentifiedWebSocket() {
        int shadowPercentage = RemoteConfig.libSignalWebSocketShadowingPercentage();
        if (shadowPercentage > 0) {
          return new ShadowingWebSocketConnection(
              "unauth-shadow",
              signalServiceConfigurationSupplier.get(),
              Optional.empty(),
              BuildConfig.SIGNAL_AGENT,
              healthMonitor,
              Stories.isFeatureEnabled(),
              LibSignalNetworkExtensions.createChatService(libSignalNetworkSupplier.get(), null, Stories.isFeatureEnabled()),
              shadowPercentage,
              bridge
          );
        }
        if (RemoteConfig.libSignalWebSocketEnabled()) {
          Network network = libSignalNetworkSupplier.get();
          return new LibSignalChatConnection(
              "libsignal-unauth",
              LibSignalNetworkExtensions.createChatService(network, null, Stories.isFeatureEnabled()),
              healthMonitor,
              false);
        } else {
          return new OkHttpWebSocketConnection("unidentified",
                                               signalServiceConfigurationSupplier.get(),
                                               Optional.empty(),
                                               BuildConfig.SIGNAL_AGENT,
                                               healthMonitor,
                                               Stories.isFeatureEnabled());
        }
      }
    };
  }

  @Override
  public @NonNull BillingApi provideBillingApi() {
    return BillingFactory.create(GooglePlayBillingDependencies.INSTANCE, RemoteConfig.messageBackups());
  }

  @Override
  public @NonNull ArchiveApi provideArchiveApi(@NonNull PushServiceSocket pushServiceSocket) {
    return new ArchiveApi(pushServiceSocket);
  }

  @Override
  public @NonNull KeysApi provideKeysApi(@NonNull PushServiceSocket pushServiceSocket) {
    return new KeysApi(pushServiceSocket);
  }

  @Override
  public @NonNull AttachmentApi provideAttachmentApi(@NonNull SignalWebSocket signalWebSocket, @NonNull PushServiceSocket pushServiceSocket) {
    return new AttachmentApi(signalWebSocket, pushServiceSocket);
  }

  @VisibleForTesting
  static class DynamicCredentialsProvider implements CredentialsProvider {

    @Override
    public ACI getAci() {
      return SignalStore.account().getAci();
    }

    @Override
    public PNI getPni() {
      return SignalStore.account().getPni();
    }

    @Override
    public String getE164() {
      return SignalStore.account().getE164();
    }

    @Override
    public String getPassword() {
      return SignalStore.account().getServicePassword();
    }

    @Override
    public int getDeviceId() {
      return SignalStore.account().getDeviceId();
    }
  }
}
