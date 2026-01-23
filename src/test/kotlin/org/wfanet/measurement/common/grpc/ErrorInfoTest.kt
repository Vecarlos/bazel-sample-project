// Copyright 2025 The Cross-Media Measurement Authors
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

package org.wfanet.measurement.common.grpc

import com.google.common.truth.Truth.assertThat
import com.google.protobuf.Any
import com.google.rpc.errorInfo
import com.google.rpc.status
import io.grpc.Status
import io.grpc.protobuf.StatusProto
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ErrorInfoTest {

  @Test
  fun `errorInfo is null when status has no details`() {
    val statusException = Status.INVALID_ARGUMENT.asException()
    val runtimeException = Status.INVALID_ARGUMENT.asRuntimeException()

    assertThat(statusException.errorInfo).isNull()
    assertThat(runtimeException.errorInfo).isNull()
  }

  @Test
  fun `errorInfo is extracted from status details`() {
    val info = errorInfo {
      domain = "test.domain"
      reason = "TEST_REASON"
      metadata["key"] = "value"
    }
    val statusProto = status {
      code = Status.INVALID_ARGUMENT.code.value()
      message = "bad"
      details += Any.pack(info)
    }

    val statusException = StatusProto.toStatusException(statusProto)
    val runtimeException = StatusProto.toStatusRuntimeException(statusProto)

    assertThat(statusException.errorInfo).isEqualTo(info)
    assertThat(runtimeException.errorInfo).isEqualTo(info)
  }
}
