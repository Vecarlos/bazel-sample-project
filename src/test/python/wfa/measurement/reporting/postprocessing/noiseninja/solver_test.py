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

from qpsolvers import Solution

from noiseninja.noised_measurements import Measurement
from noiseninja.noised_measurements import SetMeasurementsSpec
from noiseninja.solver import Solver
from src.main.proto.wfa.measurement.reporting.postprocessing.v2alpha import \
  report_post_processor_result_pb2

class SolverTest(unittest.TestCase):
  def test_solve_when_highs_solver_fails_to_converge(self):
    spec = SetMeasurementsSpec()
    spec.add_subset_relation(1, 2)
    spec.add_subset_relation(1, 3)
    spec.add_cover(1, [2, 3])
    spec.add_equal_relation(1, [4])
    spec.add_measurement(1, Measurement(50, 1, "measurement_01"))
    spec.add_measurement(2, Measurement(48, 0, "measurement_02"))
    spec.add_measurement(3, Measurement(1, 1, "measurement_03"))
    spec.add_measurement(4, Measurement(51, 1, "measurement_04"))

    solver = Solver(spec)

   

if __name__ == "__main__":
  unittest.main()
