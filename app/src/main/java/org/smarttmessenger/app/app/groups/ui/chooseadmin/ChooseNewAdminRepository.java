package com.smarttmessenger.app.groups.ui.chooseadmin;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.smarttmessenger.app.groups.GroupChangeException;
import com.smarttmessenger.app.groups.GroupId;
import com.smarttmessenger.app.groups.GroupManager;
import com.smarttmessenger.app.groups.ui.GroupChangeFailureReason;
import com.smarttmessenger.app.groups.ui.GroupChangeResult;
import com.smarttmessenger.app.recipients.RecipientId;

import java.io.IOException;
import java.util.List;

public final class ChooseNewAdminRepository {
  private final Application context;

  ChooseNewAdminRepository(@NonNull Application context) {
    this.context = context;
  }

  @WorkerThread
  @NonNull GroupChangeResult updateAdminsAndLeave(@NonNull GroupId.V2 groupId, @NonNull List<RecipientId> newAdminIds) {
    try {
      GroupManager.addMemberAdminsAndLeaveGroup(context, groupId, newAdminIds);
      return GroupChangeResult.SUCCESS;
    } catch (GroupChangeException | IOException e) {
      return GroupChangeResult.failure(GroupChangeFailureReason.fromException(e));
    }
  }
}
