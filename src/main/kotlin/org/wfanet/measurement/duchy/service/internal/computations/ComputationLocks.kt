// Copyright 2026 The Cross-Media Measurement Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.wfanet.measurement.duchy.service.internal.computations

import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * In-process lock manager keyed by global computation ID.
 *
 * This is intended to serialize concurrent updates in tests to avoid editVersion races.
 */
class ComputationLocks {
  private val locks = ConcurrentHashMap<String, Mutex>()

  suspend fun <T> withLock(globalId: String, block: suspend () -> T): T {
    val mutex = locks.computeIfAbsent(globalId) { Mutex() }
    return mutex.withLock { block() }
  }
}
