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

package org.wfanet.measurement.kingdom.service.api.v2alpha

import com.google.common.truth.Truth.assertThat
import com.google.protobuf.Any
import com.google.rpc.errorInfo
import com.google.rpc.status
import io.grpc.Status
import io.grpc.protobuf.StatusProto
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.wfanet.measurement.common.grpc.errorInfo
import org.wfanet.measurement.internal.kingdom.ErrorCode

@RunWith(JUnit4::class)
class ErrorsTest {

  @Test
  fun `toExternalStatusRuntimeException leaves status unchanged when errorInfo missing`() {
    val internalException = Status.INVALID_ARGUMENT.asException()
    val externalStatus = Status.INVALID_ARGUMENT.withDescription("bad request")

    val result = externalStatus.toExternalStatusRuntimeException(internalException)

    assertThat(result.status.code).isEqualTo(Status.INVALID_ARGUMENT.code)
    assertThat(result.status.description).isEqualTo("bad request")
    assertThat(result.errorInfo).isNull()
  }

  @Test
  fun `toExternalStatusRuntimeException maps internal errorInfo to external domain`() {
    val internalInfo = errorInfo {
      domain = ErrorCode.getDescriptor().fullName
      reason = ErrorCode.REQUIRED_FIELD_NOT_SET.name
      metadata["field_name"] = "foo"
    }
    val statusProto = status {
      code = Status.INVALID_ARGUMENT.code.value()
      message = "internal"
      details += Any.pack(internalInfo)
    }
    val internalException = StatusProto.toStatusException(statusProto)
    val externalStatus = Status.INVALID_ARGUMENT.withDescription("bad request")

    val result = externalStatus.toExternalStatusRuntimeException(internalException)
    val resultErrorInfo = result.errorInfo

    requireNotNull(resultErrorInfo)
    assertThat(resultErrorInfo.domain).isEqualTo(Errors.DOMAIN)
    assertThat(resultErrorInfo.reason).isEqualTo(ErrorCode.REQUIRED_FIELD_NOT_SET.name)
    assertThat(resultErrorInfo.metadataMap["fieldName"]).isEqualTo("foo")
    assertThat(result.status.description).isEqualTo("bad request")
  }
}
