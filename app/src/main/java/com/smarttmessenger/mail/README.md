# Mail (`com.smarttmessenger.mail`)

Replaces the **Stories** tab with a **Mail** tab. Mail is fetched in the background and only
*revealed* to the user when a delivery window opens — the exact same "held until the window opens"
semantics as the communication-window feature. This package intentionally **reuses**
`CommunicationWindowSchedule` so mail and messages share one scheduling model.

> Branch: `mail-window-v1` (off `communication-window-7.51.2`). Additive only so far — no files that
> the original author wrote have been modified. The Stories→Mail tab swap (the one destructive step)
> is documented below and left for a reviewed commit.

## What's here now (scaffold, compiles standalone)

```
model/
  MailProvider.kt          GMAIL / OUTLOOK
  MailAccount.kt           connected account + its MailDeliveryWindow
  MailDeliveryWindow.kt    reuses CommunicationWindowSchedule (held/deliver semantics)
  MailMessage.kt           normalized message (Gmail API / Graph), held flag
repository/
  MailRepository.kt        local-first, RxJava3 — mirrors CommunicationWindowsRepository
viewmodel/
  MailLandingViewModel.kt  reactive LiveData state (delivered list + waiting count)
ui/
  MailActivity.kt          hosts R.navigation.smartt_mail_navigation (copy of CommunicationWindowsActivity)
  ConnectMailAccountFragment.kt   provider picker (DSLSettings; TODO real OAuth on click)
  EditMailDeliveryScheduleFragment.kt  copy of EditCommunicationWindowScheduleFragment (same layout)
  MailReadyFragment.kt     copy of CommunicationWindowCreatedFragment (same layout)
  MailLandingFragment.kt   the future Mail tab content (DSLSettings)
```

The repository is backed by in-memory lists on purpose, so the UI can be built and demoed before the
DB / network / OAuth are wired.

## Mapping to the communication-window feature

| Mail                        | Communication window (author's code)          |
|-----------------------------|------------------------------------------------|
| `MailRepository`            | `CommunicationWindowsRepository` (local-first) |
| `MailDeliveryWindow`        | `CommunicationWindow` + `...Schedule` (reused) |
| held → delivered            | `SmarttWindowHeldState` / held table           |
| `MailLandingViewModel`      | `CommunicationWindowDetailsViewModel`          |
| `MailLandingFragment` (todo)| `StoriesLandingFragment` (`DSLSettingsFragment`)|
| `MailActivity` (todo)       | `CommunicationWindowsActivity`                 |

## Remaining work (needs the Android toolchain / server contracts)

1. **Local DB (mirror the author's tables).** Add `SmarttMailTable` + `SmarttMailHeldTable` and a
   `SmarttMailObserver`, following `SmarttCommunicationWindowsTable` / `SmarttWindowHeldTable` /
   `SmarttWindowsObserver`, and swap the in-memory stores in `MailRepository` for them.

2. **Auth + fetch (Android OAuth).** Different from the React prototype's browser OAuth:
   - **Gmail** — OAuth2 via AppAuth (`net.openid:appauth`) or Google Sign-In; then Gmail REST
     (`users.messages.list` / `.get`) over `AppDependencies.signalOkHttpClient`. Scope
     `https://www.googleapis.com/auth/gmail.readonly`.
   - **Outlook** — MSAL Android (`com.microsoft.identity.client:msal`); then Microsoft Graph
     (`/me/mailFolders/inbox/messages`). Scope `Mail.Read`.
   Implement behind `MailRepository.sync()`; store new messages `held = true`.

3. **UI — DONE (this commit).** Wizard: connect -> delivery window -> ready -> landing, in
   `R.navigation.smartt_mail_navigation`. The schedule + ready screens reuse the author's exact
   layouts (`fragment_edit_notification_profile_schedule`, `fragment_notification_profile_created`),
   so they are pixel-identical to the communication-window wizard. Strings live in the new
   `res/values/smartt_mail_strings.xml`. Launch with `MailActivity.newIntentForConnect(context)`.
   Still to do here: real OAuth in `ConnectMailAccountFragment.pick()` (currently records a demo
   address), a richer message list row (currently `textPref`), and a `res/raw/mail_28` Lottie icon
   for the tab (mirror `stories_28`).

4. **Tab swap — Stories → Mail (the only destructive step; do in its own reviewed commit).**
   All in `app/src/main/java/org/thoughtcrime/securesms/main/`:
   - `MainNavigation.kt` — in `enum MainNavigationListLocation`, replace the `STORIES` entry
     (`label = R.string.ConversationListTabs__stories`, `icon = R.raw.stories_28`) with `MAIL`
     (`R.string.ConversationListTabs__mail`, `R.raw.mail_28`); rename `storiesCount` → `mailCount`
     in the state and the `when` branches; the `filterNot { it == STORIES }` guards become `MAIL`
     (or drop the guard if Mail is always shown).
   - `MainNavigationViewModel.kt` — `onStoriesSelected()` → `onMailSelected()`;
     `getNumberOfUnseenStories` → unseen-mail flow; `isStoriesFeatureEnabled` → `isMailFeatureEnabled`.
   - `MainNavigationRepository.kt` — replace `getNumberOfUnseenStories()` /
     `getHasFailedOutgoingStories()` with mail equivalents backed by `MailRepository`.
   - `MainToolbarViewModel.kt` — `presentToolbarForStoriesLandingFragment()` → a Mail equivalent.
   - Point the tab's content Fragment at `MailLandingFragment` instead of `StoriesLandingFragment`.

## Note

This was written to match the author's conventions but **has not been compiled here** (no Android
SDK/Gradle build in this environment). Build in Android Studio; the model/repository/viewmodel layer
only depends on RxJava3 + androidx.lifecycle + `org.signal.core.util.logging.Log`.
