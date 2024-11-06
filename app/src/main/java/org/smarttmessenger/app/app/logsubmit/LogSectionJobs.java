package com.smarttmessenger.app.logsubmit;

import android.content.Context;

import androidx.annotation.NonNull;

import com.smarttmessenger.app.dependencies.AppDependencies;

public class LogSectionJobs implements LogSection {

  @Override
  public @NonNull String getTitle() {
    return "JOBS";
  }

  @Override
  public @NonNull CharSequence getContent(@NonNull Context context) {
    return AppDependencies.getJobManager().getDebugInfo();
  }
}
