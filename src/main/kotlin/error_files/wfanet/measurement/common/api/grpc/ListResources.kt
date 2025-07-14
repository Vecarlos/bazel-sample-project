/*
 * Copyright 2024 The Cross-Media Measurement Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wfanet.measurement.common.api.grpc

import com.google.protobuf.Message
import io.grpc.kotlin.AbstractCoroutineStub
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/** A [List] of resources from a paginated List method. */
data class ResourceList<R : Message, T>(
  val resources: List<R>,
  /**
   * A token that can be sent on subsequent requests to retrieve the next page. If this is an empty
   * page token, there are no subsequent pages.
   *
   * If [T] is [String], then the empty page token is `""`. If [T] is nullable, then the empty page
   * token is `null`.
   */
  val nextPageToken: T,
) : List<R> by resources

/**
 * Lists resources from a paginated List method on this stub.
 *
 * @param initialPageToken page token for initial request.
 * @param emptyPageToken The value that represents an empty/final page token (e.g., "" or null).
 * @param list function which calls the appropriate List method on the stub.
 */
// 1. Se eliminaron 'inline' y 'reified'.
// 2. Se eliminó el valor por defecto de 'initialPageToken'.
// 3. Se añadió el parámetro 'emptyPageToken'.
// 4. Se eliminó 'crossinline'.
fun <R : Message, T, S : AbstractCoroutineStub<S>> S.listResources(
  initialPageToken: T,
  emptyPageToken: T,
  list: suspend S.(pageToken: T) -> ResourceList<R, T>,
): Flow<ResourceList<R, T>> =
  listResources(Int.MAX_VALUE, initialPageToken, emptyPageToken) { nextPageToken, _ ->
    list(nextPageToken)
  }

/**
 * Lists resources from a paginated List method on this stub.
 *
 * @param limit maximum number of resources to emit.
 * @param initialPageToken page token for initial request.
 * @param emptyPageToken The value that represents an empty/final page token (e.g., "" or null).
 * @param list function which calls the appropriate List method on the stub, returning no more than
 * the specified remaining number of resources.
 */
// 1. Se eliminaron 'inline' y 'reified'.
// 2. Se eliminó el valor por defecto de 'initialPageToken'.
// 3. Se añadió el parámetro 'emptyPageToken'.
// 4. Se eliminó 'crossinline'.
fun <R : Message, T, S : AbstractCoroutineStub<S>> S.listResources(
  limit: Int,
  initialPageToken: T,
  emptyPageToken: T,
  list: suspend S.(pageToken: T, remaining: Int) -> ResourceList<R, T>,
): Flow<ResourceList<R, T>> {
  require(limit > 0) { "limit must be positive" }
  return flow {
    var remaining: Int = limit
    var nextPageToken = initialPageToken

    while (true) {
      coroutineContext.ensureActive()

      val resourceList: ResourceList<R, T> = list(nextPageToken, remaining)
      require(resourceList.size <= remaining) {
        "List call must ensure that limit is not exceeded. " +
          "Returned ${resourceList.size} items when only $remaining were remaining"
      }
      emit(resourceList)

      remaining -= resourceList.size
      nextPageToken = resourceList.nextPageToken
      // 5. Se usa el parámetro 'emptyPageToken' en lugar de llamar a una función.
      if (nextPageToken == emptyPageToken || remaining == 0) {
        break
      }
    }
  }
}

/** @see [flattenConcat] */
@ExperimentalCoroutinesApi // Overloads experimental `flattenConcat` function.
fun <R : Message, T> Flow<ResourceList<R, T>>.flattenConcat(): Flow<R> =
  map { it.asFlow() }.flattenConcat()

// 6. La función 'getEmptyPageToken' ya no es necesaria y se ha eliminado.