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

import math
import numpy as np
import random
from functools import reduce
from itertools import combinations
from typing import Any
from typing import FrozenSet
from typing import Optional
from typing import Tuple
from typing import TypeAlias

from absl import logging
from qpsolvers import Solution

from noiseninja.noised_measurements import KReachMeasurements
from noiseninja.noised_measurements import Measurement
from noiseninja.noised_measurements import MeasurementSet
from noiseninja.noised_measurements import OrderedSets
from noiseninja.noised_measurements import SetMeasurementsSpec
from noiseninja.solver import Solver
from src.main.proto.wfa.measurement.reporting.postprocessing.v2alpha import \
  report_post_processor_result_pb2

SEED = 10
random.seed(SEED)
np.random.seed(SEED)
ReportPostProcessorResult = report_post_processor_result_pb2.ReportPostProcessorResult
ReportPostProcessorStatus = report_post_processor_result_pb2.ReportPostProcessorStatus
ReportQuality = report_post_processor_result_pb2.ReportQuality

EdpCombination: TypeAlias = FrozenSet[str]




class Report:
  """Represents a full report with multiple MetricReports and set relationships.

    This class aggregates multiple MetricReport objects, and the subset relation
    between the the metrics.

    Attributes:
        _metric_reports: A dictionary mapping metric names (e.g., "MRC", "AMI")
                         to their corresponding MetricReport objects.
        _metric_subsets_by_parent: A dictionary defining subset relationships
                                   between metrics. Each key is a parent metric,
                                   and the value is a list of its child metrics.
        _cumulative_inconsistency_allowed_edp_combinations: A set of EDP
                                                            combinations for
                                                            which inconsistencies
                                                            in cumulative
                                                            measurements are
                                                            allowed. This is for
                                                            TV measurements.
        _population_size: The size of the population.
    """

  def __init__(
      self,
      metric_reports: dict[str, int],
      metric_subsets_by_parent: dict[str, list[str]],
      cumulative_inconsistency_allowed_edp_combinations: set[str],
      population_size: float = 0.0,
  ):
    """
    Args:
        metric_reports: a dictionary mapping metric types to a MetricReport
        metric_subsets_by_parent: a dictionary containing subset
            relationship between the metrics. .e.g. ami >= [custom, mrc]
        cumulative_inconsistency_allowed_edps : a set containing edp keys that won't
            be forced to have self cumulative reaches be increasing
    """
    self._metric_reports = metric_reports
    self._metric_subsets_by_parent = metric_subsets_by_parent
    self._cumulative_inconsistency_allowed_edp_combinations = (
        cumulative_inconsistency_allowed_edp_combinations
    )
    self._population_size = population_size
