package com.smarttmessenger.app.profiles.spoofing;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.annimon.stream.Stream;

import com.smarttmessenger.app.database.SignalDatabase;
import com.smarttmessenger.app.database.model.GroupRecord;
import com.smarttmessenger.app.recipients.Recipient;
import com.smarttmessenger.app.recipients.RecipientId;

public final class ReviewUtil {

  private ReviewUtil() { }

  @WorkerThread
  public static int getGroupsInCommonCount(@NonNull Context context, @NonNull RecipientId recipientId) {
    return Stream.of(SignalDatabase.groups()
                 .getPushGroupsContainingMember(recipientId))
                 .filter(g -> g.getMembers().contains(Recipient.self().getId()))
                 .map(GroupRecord::getRecipientId)
                 .toList()
                 .size();
  }
}
