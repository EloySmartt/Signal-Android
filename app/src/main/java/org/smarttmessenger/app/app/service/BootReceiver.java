package com.smarttmessenger.app.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.smarttmessenger.app.dependencies.AppDependencies;
import com.smarttmessenger.app.jobs.MessageFetchJob;

public class BootReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    AppDependencies.getJobManager().add(new MessageFetchJob());
  }
}
