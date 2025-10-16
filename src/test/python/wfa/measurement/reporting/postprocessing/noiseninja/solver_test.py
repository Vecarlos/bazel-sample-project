# Copyright 2024 The Cross-Media Measurement Authors
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import unittest
from unittest.mock import MagicMock
from noiseninja.solver import Solver
from src.main.proto.wfa.measurement.reporting.postprocessing.v2alpha import \
  report_post_processor_result_pb2

ReportPostProcessorStatus = report_post_processor_result_pb2.ReportPostProcessorStatus
StatusCode = ReportPostProcessorStatus.StatusCode

HIGHS_SOLVER = "highs"
TOLERANCE = 1e-1


class SolverTest(unittest.TestCase):


  def test_solve_when_highs_solver_fails_to_converge(self):
    spec = [[1, [4]]]
    solver = Solver(spec)

 
    self.assertEqual(1,1)

if __name__ == "__main__":
  unittest.main()
