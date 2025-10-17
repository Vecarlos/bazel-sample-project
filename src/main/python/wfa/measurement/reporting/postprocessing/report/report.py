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


def fuzzy_equal(val: float, target: float, tolerance: float) -> bool:
  """Checks if two float values are approximately equal within an absolute tolerance."""
  return math.isclose(val, target, rel_tol=0.0, abs_tol=tolerance)


def fuzzy_less_equal(smaller: float, larger: float, tolerance: float) -> bool:
  """Checks if one float value is less than or equal to another within a tolerance."""
  return larger - smaller + tolerance >= 0


def get_subset_relationships(
    edp_combinations: list[EdpCombination],
) -> list[Tuple[EdpCombination, EdpCombination]]:
  """Returns a list of tuples where first element in the tuple is the parent
  and second element is the subset."""
  # logging.debug(
  #     "Getting subset relations for the list of EDP combinations "
  #     f"{edp_combinations}."
  # )
  subset_relationships = []
  for comb1, comb2 in combinations(edp_combinations, 2):
    if comb1.issubset(comb2):
      subset_relationships.append((comb2, comb1))
    elif comb2.issubset(comb1):
      subset_relationships.append((comb1, comb2))
  # logging.debug(
  #     f"The subset relationships for {edp_combinations} are "
  #     f"{subset_relationships}."
  # )
  return subset_relationships


def is_cover(
    target_set: EdpCombination, possible_cover: list[EdpCombination]
) -> bool:
  """Checks if a collection of sets covers a target set.

  Args:
    target_set: The set that should be covered.
    possible_cover: A collection of sets that may cover the target set.

  Returns:
    True if the union of the sets in `possible_cover` equals `target_set`,
    False otherwise.
  """
  union_of_possible_cover = reduce(
      lambda x, y: x.union(y), possible_cover
  )
  return union_of_possible_cover == target_set


def get_covers(
    target_set: EdpCombination, other_sets: list[EdpCombination]
) -> list[Tuple[EdpCombination, list[EdpCombination]]]:
  """Finds all combinations of sets from `other_sets` that cover `target_set`.

  This function identifies all possible combinations of sets within `other_sets`
  whose union equals the `target_set`. It only considers sets that are subsets of
  the `target_set`.

  Args:
    target_set: The set that needs to be covered.
    other_sets: A collection of sets that may be used to cover the `target_set`.

  Returns:
    A list of tuples, where each tuple represents a covering relationship.
    The first element of the tuple is the `target_set`, and the second element
    is a tuple containing the sets from `other_sets` that cover it.
  """
  # logging.debug(f"Getting cover relations for {target_set} from {other_sets}.")

  def generate_all_length_combinations(data: list[Any]) -> list[
    tuple[Any, ...]]:
    """Generates all possible combinations of elements from a list.

    Args:
      data: A list of elements.

    Returns:
      A list of tuples, where each tuple represents a combination of elements.
    """
    return [
        comb for r in range(1, len(data) + 1) for comb in
        combinations(data, r)
    ]

  cover_relationship = []
  all_subsets_of_possible_covered = [other_set for other_set in other_sets
                                     if
                                     other_set.issubset(target_set)]
  possible_covers = generate_all_length_combinations(
      all_subsets_of_possible_covered)
  for possible_cover in possible_covers:
    if is_cover(target_set, possible_cover):
      cover_relationship.append((target_set, possible_cover))
  logging.debug(
      f"The cover relationship is {cover_relationship}."
  )
  return cover_relationship


def get_cover_relationships(
    edp_combinations: list[EdpCombination],
) -> list[Tuple[EdpCombination, list[EdpCombination]]]:
  """Returns covers as defined here: # https://en.wikipedia.org/wiki/Cover_(topology).
  For each set (s_i) in the list, enumerate combinations of all sets excluding this one.
  For each of these considered combinations, take their union and check if it is equal to
  s_i. If so, this combination is a cover of s_i.
  """
  # logging.debug(
  #     "Getting all cover relationships from a list of EDP combinations "
  #     f"{edp_combinations}"
  # )
  cover_relationships = []
  for i in range(len(edp_combinations)):
    possible_covered = edp_combinations[i]
    other_sets = edp_combinations[:i] + edp_combinations[i + 1:]
    cover_relationship = get_covers(possible_covered, other_sets)
    cover_relationships.extend(cover_relationship)
  return cover_relationships


def is_union_reach_consistent(
    union_measurement: Measurement,
    component_measurements: list[Measurement], population_size: float) -> bool:
  """Verifies that the expected union reach is statistically consistent with
  individual EDP measurements assuming conditional independence between the sets
  of VIDs reached by the different EDPs.

  The check is done by comparing the absolute difference between the observed
  union reach and the expected union reach against a confidence range.

  Let U be the population size, X_1, ..., X_n be the single EDPs. If the reach
  of the EDPs are independent of one another, the expected union reach is:
      |X_1 union … union Xn_| = U - (U - |X_1|)...(U - |X_n|)/U^{n-1}

  Let D = expected union - measuremed union.

  The standard deviation of the difference between the expected union reach
  and the measured union reach is bounded by
  std(D) <= sqrt(var(|X_1|) + ... + var(|X_n|) + var(measured union)).

  Returns:
    True if D is in [-7*std(D); 7*std(D)].
    False otherwise.
  """

  if population_size <= 0:
    raise ValueError(
        f"The population size must be greater than 0, but got"
        f" {population_size}."
    )

  if len(component_measurements) <= 1:
    raise ValueError(
        f"The length of individual reaches must be at least 2, but got"
        f" {len(component_measurements)}."
    )

  variance = union_measurement.sigma ** 2

  probability = 1.0

  for measurement in component_measurements:
    probability *= max(0.0, 1.0 - measurement.value / population_size)
    variance += measurement.sigma ** 2

  probability = min(1.0, probability)

  expected_union_measurement = population_size * (1.0 - probability)

  # An upperbound of STDDEV(expected union - measured union).
  standard_deviation = np.sqrt(variance)

  return abs(expected_union_measurement - union_measurement.value) <= \
    STANDARD_DEVIATION_TEST_THRESHOLD * standard_deviation


def get_edps_from_edp_combination(
    edp_combination: EdpCombination,
    all_edp_combinations: set[EdpCombination]
) -> list[EdpCombination]:
  return list(
    all_edp_combinations.intersection([frozenset({edp}) for edp in edp_combination])
  )



# def build_measurement_set(
#     reach: dict[EdpCombination, Measurement],
#     k_reach: dict[EdpCombination, KReachMeasurements],
#     impression: dict[EdpCombination, Measurement]
# ) -> dict[EdpCombination, MeasurementSet]:
#   """Builds a dictionary of MeasurementSet from separate measurement dicts."""
#   all_edps = (
#       set(reach.keys())
#       | set(k_reach.keys())
#       | set(impression.keys())
#   )
#   whole_campaign_measurements = {}
#   for edp in all_edps:
#     whole_campaign_measurements[edp] = MeasurementSet(
#         reach=reach.get(edp),
#         k_reach=k_reach.get(edp, {}),
#         impression=impression.get(edp),
#     )
#   return whole_campaign_measurements




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
