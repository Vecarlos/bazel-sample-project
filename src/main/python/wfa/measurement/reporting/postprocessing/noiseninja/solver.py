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


class Solver:

  def __init__(self):
    variable_index_by_set_id = {1: 0, 2: 1, 3: 2, 4: 3}
    self.num_variables = len(variable_index_by_set_id)
  
    self._add_equals(variable_index_by_set_id)

  def _add_equals(self,variable_index_by_set_id):
    variables =  np.zeros(self.num_variables)
    equal_set = [1, [4]]
