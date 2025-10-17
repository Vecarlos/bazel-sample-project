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

MIN_STANDARD_VARIATION_RATIO = 0.001
UNIT_SCALING_FACTOR = 1.0
TOLERANCE = 1e-6

# The probability of a value falling outside the [-7*STDDEV; 7*STDDEV] range is
# approximately 2^{-38.5}.
STANDARD_DEVIATION_TEST_THRESHOLD = 7.0

CONSISTENCY_TEST_TOLERANCE = 1.0


# def fuzzy_equal(val: float, target: float, tolerance: float) -> bool:
#   """Checks if two float values are approximately equal within an absolute tolerance."""
#   return math.isclose(val, target, rel_tol=0.0, abs_tol=tolerance)


# def fuzzy_less_equal(smaller: float, larger: float, tolerance: float) -> bool:
#   """Checks if one float value is less than or equal to another within a tolerance."""
#   return larger - smaller + tolerance >= 0


# def get_subset_relationships(
#     edp_combinations: list[EdpCombination],
# ) -> list[Tuple[EdpCombination, EdpCombination]]:
#   """Returns a list of tuples where first element in the tuple is the parent
#   and second element is the subset."""
#   # logging.debug(
#   #     "Getting subset relations for the list of EDP combinations "
#   #     f"{edp_combinations}."
#   # )
#   subset_relationships = []
#   for comb1, comb2 in combinations(edp_combinations, 2):
#     if comb1.issubset(comb2):
#       subset_relationships.append((comb2, comb1))
#     elif comb2.issubset(comb1):
#       subset_relationships.append((comb1, comb2))
#   # logging.debug(
#   #     f"The subset relationships for {edp_combinations} are "
#   #     f"{subset_relationships}."
#   # )
#   return subset_relationships


# def is_cover(
#     target_set: EdpCombination, possible_cover: list[EdpCombination]
# ) -> bool:
#   """Checks if a collection of sets covers a target set.

#   Args:
#     target_set: The set that should be covered.
#     possible_cover: A collection of sets that may cover the target set.

#   Returns:
#     True if the union of the sets in `possible_cover` equals `target_set`,
#     False otherwise.
#   """
#   union_of_possible_cover = reduce(
#       lambda x, y: x.union(y), possible_cover
#   )
#   return union_of_possible_cover == target_set




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
