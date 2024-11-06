/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.util

import com.smarttmessenger.app.dependencies.AppDependencies
import com.smarttmessenger.app.jobmanager.Job
import com.smarttmessenger.app.jobmanager.JobManager

/** Starts a new chain with this job. */
fun Job.asChain(): JobManager.Chain {
  return AppDependencies.jobManager.startChain(this)
}
