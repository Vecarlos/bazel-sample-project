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


class Report:


  def __init__(self):
    pass