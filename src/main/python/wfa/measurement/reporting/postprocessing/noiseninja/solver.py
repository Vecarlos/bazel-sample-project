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

import numpy as np
from threading import Semaphore


from qpsolvers import Solution

from noiseninja.noised_measurements import SetMeasurementsSpec

SEMAPHORE = Semaphore()


class SolutionNotFoundError(ValueError):
  _non_solution: Solution

  def __init__(self, non_solution: Solution):
    super().__init__(non_solution)
    self._non_solution = non_solution

  def get_non_solution_details(self):
    return self._non_solution


class Solver:

  def __init__(self, set_measurement_spec: SetMeasurementsSpec):
    variable_index_by_set_id = {1: 0, 2: 1, 3: 2, 4: 3}
    self.num_variables = len(variable_index_by_set_id)
  
    self._add_equals(set_measurement_spec, variable_index_by_set_id)

  def _add_equals(self, set_measurement_spec: SetMeasurementsSpec,
      variable_index_by_set_id: dict[int, int]):

    for equal_set in set_measurement_spec:
      
      variables = np.zeros(self.num_variables)
      variables[variable_index_by_set_id[equal_set[0]]] = 1
 
      variables.put([variable_index_by_set_id[i] for i in equal_set[1]], -1)
      self._add_eq_term(variables, 0)


  def _add_eq_term(self, variables: np.array, k: float):
    pass
