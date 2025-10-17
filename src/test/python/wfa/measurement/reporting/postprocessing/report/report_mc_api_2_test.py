# Copyright 2025 The Cross-Media Measurement Authors
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

# from noiseninja.noised_measurements import Measurement
# from noiseninja.noised_measurements import MeasurementSet
# from noiseninja.noised_measurements import OrderedSets
# from noiseninja.noised_measurements import SetMeasurementsSpec

from report.report import MetricReport
from report.report import Report
from report.report import build_measurement_set
from report.testing.validate_report import get_sorted_list
from report.testing.validate_report import ordered_sets_to_sorted_list



class TestReportMcApi2(unittest.TestCase):
    def aways_true(self):
        return self.assertEqual(1,1)



if __name__ == "__main__":
    unittest.main()
