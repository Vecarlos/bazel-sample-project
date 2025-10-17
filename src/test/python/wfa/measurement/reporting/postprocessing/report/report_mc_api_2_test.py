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

from noiseninja.noised_measurements import Measurement
from noiseninja.noised_measurements import MeasurementSet
from noiseninja.noised_measurements import OrderedSets
from noiseninja.noised_measurements import SetMeasurementsSpec

from report.report import MetricReport
from report.report import Report
from report.report import build_measurement_set
from report.testing.validate_report import get_sorted_list
from report.testing.validate_report import ordered_sets_to_sorted_list

from src.main.proto.wfa.measurement.reporting.postprocessing.v2alpha import \
  report_post_processor_result_pb2

StatusCode = report_post_processor_result_pb2.ReportPostProcessorStatus.StatusCode
ReportQuality = report_post_processor_result_pb2.ReportQuality

# EXPECTED_PRECISION = 1
# EDP_ONE = "EDP_ONE"
# EDP_TWO = "EDP_TWO"
# EDP_THREE = "EDP_THREE"

# NOISE_CORRECTION_TOLERANCE = 0.1

# SAMPLE_REPORT = Report(
#     metric_reports={
#         "ami":
#             MetricReport(
#                 weekly_cumulative_reaches={
#                     frozenset({EDP_ONE}): [
#                         Measurement(9992500, 10000, "m_001"),
#                         Measurement(11998422, 10000, "m_002")
#                     ],
#                     frozenset({EDP_TWO}): [
#                         Measurement(5000000, 0, "m_003"),
#                         Measurement(6000000, 0, "m_004")
#                     ],
#                     frozenset({EDP_THREE}): [
#                         Measurement(800000, 0, "m_005"),
#                         Measurement(1000000, 0, "m_006")
#                     ],
#                     frozenset({EDP_ONE, EDP_TWO, EDP_THREE}): [
#                         Measurement(15830545, 10000, "m_007"),
#                         Measurement(19010669, 10000, "m_008")
#                     ],
#                 },
#                 whole_campaign_measurements=build_measurement_set(
#                     reach={
#                         frozenset({EDP_ONE}):
#                             Measurement(11978894, 10000, "m_009"),
#                         frozenset({EDP_TWO}):
#                             Measurement(6000000, 0, "m_010"),
#                         frozenset({EDP_THREE}):
#                             Measurement(1000000, 0, "m_011"),
#                         frozenset({EDP_ONE, EDP_TWO}):
#                             Measurement(16686873, 10000, "m_012"),
#                         frozenset({EDP_ONE, EDP_TWO, EDP_THREE}):
#                             Measurement(19021738, 10000, "m_013"),
#                     },
#                     k_reach={
#                         frozenset({EDP_ONE}): {
#                             1: Measurement(6182655, 10000, "m_014"),
#                             2: Measurement(3091328, 10000, "m_015"),
#                             3: Measurement(1545664, 10000, "m_016"),
#                             4: Measurement(772832, 10000, "m_017"),
#                             5: Measurement(386415, 10000, "m_018"),
#                         },
#                         frozenset({EDP_TWO}): {
#                             1: Measurement(3096774, 0, "m_019"),
#                             2: Measurement(1548387, 0, "m_020"),
#                             3: Measurement(774194, 0, "m_021"),
#                             4: Measurement(387097, 0, "m_022"),
#                             5: Measurement(193548, 0, "m_023"),
#                         },
#                         frozenset({EDP_THREE}): {
#                             1: Measurement(516129, 0, "m_024"),
#                             2: Measurement(258065, 0, "m_025"),
#                             3: Measurement(129033, 0, "m_026"),
#                             4: Measurement(64517, 0, "m_027"),
#                             5: Measurement(32256, 0, "m_028"),
#                         },
#                         frozenset({EDP_ONE, EDP_TWO, EDP_THREE}): {
#                             1: Measurement(9817671, 10000, "m_029"),
#                             2: Measurement(4908836, 10000, "m_030"),
#                             3: Measurement(2454418, 10000, "m_031"),
#                             4: Measurement(1227209, 10000, "m_032"),
#                             5: Measurement(613604, 10000, "m_033"),
#                         },
#                     },
#                     impression={
#                         frozenset({EDP_ONE}):
#                             Measurement(22870892, 10000, "m_034"),
#                         frozenset({EDP_TWO}):
#                             Measurement(11216125, 0, "m_035"),
#                         frozenset({EDP_THREE}):
#                             Measurement(1844136, 0, "m_036"),
#                         frozenset({EDP_ONE, EDP_TWO}):
#                             Measurement(34113188, 10000, "m_037"),
#                         frozenset({EDP_ONE, EDP_TWO, EDP_THREE}):
#                             Measurement(35926461, 10000, "m_038"),
#                     }),
#                 weekly_non_cumulative_measurements={
#                     frozenset({EDP_ONE}): [
#                         MeasurementSet(
#                             reach=Measurement(10008130, 10000, "m_039"),
#                             k_reach={
#                                 1: Measurement(5165486, 10000, "m_040"),
#                                 2: Measurement(2582743, 10000, "m_041"),
#                                 3: Measurement(1291372, 10000, "m_042"),
#                                 4: Measurement(645686, 10000, "m_043"),
#                                 5: Measurement(322843, 10000, "m_044"),
#                             },
#                             impression=Measurement(18379493, 10000, "m_045"),
#                         ),
#                         MeasurementSet(
#                             reach=Measurement(2452001, 10000, "m_046"),
#                             k_reach={
#                                 1: Measurement(1265549, 10000, "m_047"),
#                                 2: Measurement(632775, 10000, "m_048"),
#                                 3: Measurement(316388, 10000, "m_049"),
#                                 4: Measurement(158194, 10000, "m_050"),
#                                 5: Measurement(79095, 10000, "m_051"),
#                             },
#                             impression=Measurement(4471035, 10000, "m_052"),
#                         )
#                     ],
#                     frozenset({EDP_TWO}): [
#                         MeasurementSet(
#                             reach=Measurement(5000000, 0, "m_053"),
#                             k_reach={
#                                 1: Measurement(2580645, 0, "m_054"),
#                                 2: Measurement(1290323, 0, "m_055"),
#                                 3: Measurement(645162, 0, "m_056"),
#                                 4: Measurement(322581, 0, "m_057"),
#                                 5: Measurement(161289, 0, "m_058"),
#                             },
#                             impression=Measurement(9193546, 0, "m_059"),
#                         ),
#                         MeasurementSet(
#                             reach=Measurement(1100000, 0, "m_060"),
#                             k_reach={
#                                 1: Measurement(567742, 0, "m_061"),
#                                 2: Measurement(283871, 0, "m_062"),
#                                 3: Measurement(141936, 0, "m_063"),
#                                 4: Measurement(70968, 0, "m_064"),
#                                 5: Measurement(35483, 0, "m_065"),
#                             },
#                             impression=Measurement(2022579, 0, "m_066"),
#                         )
#                     ],
#                     frozenset({EDP_THREE}): [
#                         MeasurementSet(
#                             reach=Measurement(800000, 0, "m_067"),
#                             k_reach={
#                                 1: Measurement(412903, 0, "m_068"),
#                                 2: Measurement(206452, 0, "m_069"),
#                                 3: Measurement(103226, 0, "m_070"),
#                                 4: Measurement(51613, 0, "m_071"),
#                                 5: Measurement(25806, 0, "m_072"),
#                             },
#                             impression=Measurement(1470967, 0, "m_073"),
#                         ),
#                         MeasurementSet(
#                             reach=Measurement(202952, 0, "m_074"),
#                             k_reach={
#                                 1: Measurement(104749, 0, "m_075"),
#                                 2: Measurement(52375, 0, "m_076"),
#                                 3: Measurement(26188, 0, "m_077"),
#                                 4: Measurement(13094, 0, "m_078"),
#                                 5: Measurement(6546, 0, "m_079"),
#                             },
#                             impression=Measurement(373169, 0, "m_080"),
#                         )
#                     ],
#                     frozenset({EDP_ONE, EDP_TWO, EDP_THREE}): [
#                         MeasurementSet(
#                             reach=Measurement(15829304, 10000, "m_081"),
#                             k_reach={
#                                 1: Measurement(8169963, 10000, "m_082"),
#                                 2: Measurement(4084982, 10000, "m_083"),
#                                 3: Measurement(2042491, 10000, "m_084"),
#                                 4: Measurement(1021246, 10000, "m_085"),
#                                 5: Measurement(510622, 10000, "m_086"),
#                             },
#                             impression=Measurement(29046331, 10000, "m_087"),
#                         ),
#                         MeasurementSet(
#                             reach=Measurement(3761510, 10000, "m_088"),
#                             k_reach={
#                                 1: Measurement(1941425, 10000, "m_089"),
#                                 2: Measurement(970713, 10000, "m_090"),
#                                 3: Measurement(485357, 10000, "m_091"),
#                                 4: Measurement(242679, 10000, "m_092"),
#                                 5: Measurement(121336, 10000, "m_093"),
#                             },
#                             impression=Measurement(6904573, 10000, "m_094"),
#                         )
#                     ],
#                 },
#             ),
#         "mrc":
#             MetricReport(
#                 weekly_cumulative_reaches={
#                     frozenset({EDP_ONE}): [
#                         Measurement(9501618, 10000, "m_095"),
#                         Measurement(11389309, 10000, "m_096")
#                     ],
#                     frozenset({EDP_TWO}): [
#                         Measurement(4750000, 0, "m_097"),
#                         Measurement(5700000, 0, "m_098")
#                     ],
#                     frozenset({EDP_THREE}): [
#                         Measurement(760000, 0, "m_099"),
#                         Measurement(950000, 0, "m_100")
#                     ],
#                     frozenset({EDP_ONE, EDP_TWO}): [
#                         Measurement(13427250, 10000, "m_101"),
#                         Measurement(15920317, 10000, "m_102")
#                     ],
#                 },
#                 whole_campaign_measurements=build_measurement_set(
#                     reach={
#                         frozenset({EDP_ONE}):
#                             Measurement(11382243, 10000, "m_103"),
#                         frozenset({EDP_TWO}):
#                             Measurement(5700000, 0, "m_104"),
#                         frozenset({EDP_THREE}):
#                             Measurement(950000, 0, "m_105"),
#                         frozenset({EDP_ONE, EDP_TWO}):
#                             Measurement(15908881, 10000, "m_106"),
#                     },
#                     k_reach={
#                         frozenset({EDP_ONE}): {
#                             1: Measurement(5874706, 10000, "m_107"),
#                             2: Measurement(2937353, 10000, "m_108"),
#                             3: Measurement(1468677, 10000, "m_109"),
#                             4: Measurement(734339, 10000, "m_110"),
#                             5: Measurement(367168, 10000, "m_111"),
#                         },
#                         frozenset({EDP_TWO}): {
#                             1: Measurement(2941935, 0, "m_112"),
#                             2: Measurement(1470968, 0, "m_113"),
#                             3: Measurement(735484, 0, "m_114"),
#                             4: Measurement(367742, 0, "m_115"),
#                             5: Measurement(183871, 0, "m_116"),
#                         },
#                         frozenset({EDP_THREE}): {
#                             1: Measurement(490323, 0, "m_117"),
#                             2: Measurement(245162, 0, "m_118"),
#                             3: Measurement(122581, 0, "m_119"),
#                             4: Measurement(61291, 0, "m_120"),
#                             5: Measurement(30643, 0, "m_121"),
#                         },
#                         frozenset({EDP_ONE, EDP_TWO}): {
#                             1: Measurement(8211035, 10000, "m_122"),
#                             2: Measurement(4105518, 10000, "m_123"),
#                             3: Measurement(2052759, 10000, "m_124"),
#                             4: Measurement(1026380, 10000, "m_125"),
#                             5: Measurement(513189, 10000, "m_126"),
#                         },
#                     },
#                     impression={
#                         frozenset({EDP_ONE}):
#                             Measurement(21696322, 10000, "m_127"),
#                         frozenset({EDP_TWO}):
#                             Measurement(10645760, 0, "m_128"),
#                         frozenset({EDP_THREE}):
#                             Measurement(1751669, 0, "m_129"),
#                         frozenset({EDP_ONE, EDP_TWO}):
#                             Measurement(32337826, 10000, "m_130"),
#                     }),
#                 weekly_non_cumulative_measurements={
#                     frozenset({EDP_ONE}): [
#                         MeasurementSet(
#                             reach=Measurement(9503446, 10000, "m_131"),
#                             k_reach={
#                                 1: Measurement(4905004, 10000, "m_132"),
#                                 2: Measurement(2452502, 10000, "m_133"),
#                                 3: Measurement(1226251, 10000, "m_134"),
#                                 4: Measurement(613126, 10000, "m_135"),
#                                 5: Measurement(306563, 10000, "m_136"),
#                             },
#                             impression=Measurement(17473517, 10000, "m_137"),
#                         ),
#                         MeasurementSet(
#                             reach=Measurement(2289252, 10000, "m_138"),
#                             k_reach={
#                                 1: Measurement(1181549, 10000, "m_139"),
#                                 2: Measurement(590775, 10000, "m_140"),
#                                 3: Measurement(295388, 10000, "m_141"),
#                                 4: Measurement(147694, 10000, "m_142"),
#                                 5: Measurement(73846, 10000, "m_143"),
#                             },
#                             impression=Measurement(4236753, 10000, "m_144"),
#                         )
#                     ],
#                     frozenset({EDP_TWO}): [
#                         MeasurementSet(
#                             reach=Measurement(4750000, 0, "m_145"),
#                             k_reach={
#                                 1: Measurement(2451613, 0, "m_146"),
#                                 2: Measurement(1225807, 0, "m_147"),
#                                 3: Measurement(612904, 0, "m_148"),
#                                 4: Measurement(306452, 0, "m_149"),
#                                 5: Measurement(153224, 0, "m_150"),
#                             },
#                             impression=Measurement(8733867, 0, "m_151"),
#                         ),
#                         MeasurementSet(
#                             reach=Measurement(1039801, 0, "m_152"),
#                             k_reach={
#                                 1: Measurement(536671, 0, "m_153"),
#                                 2: Measurement(268336, 0, "m_154"),
#                                 3: Measurement(134168, 0, "m_155"),
#                                 4: Measurement(67084, 0, "m_156"),
#                                 5: Measurement(33542, 0, "m_157"),
#                             },
#                             impression=Measurement(1911893, 0, "m_158"),
#                         )
#                     ],
#                     frozenset({EDP_THREE}): [
#                         MeasurementSet(
#                             reach=Measurement(760000, 0, "m_159"),
#                             k_reach={
#                                 1: Measurement(392258, 0, "m_160"),
#                                 2: Measurement(196129, 0, "m_161"),
#                                 3: Measurement(98065, 0, "m_162"),
#                                 4: Measurement(49033, 0, "m_163"),
#                                 5: Measurement(24515, 0, "m_164"),
#                             },
#                             impression=Measurement(1397418, 0, "m_165"),
#                         ),
#                         MeasurementSet(
#                             reach=Measurement(192662, 0, "m_166"),
#                             k_reach={
#                                 1: Measurement(99438, 0, "m_167"),
#                                 2: Measurement(49719, 0, "m_168"),
#                                 3: Measurement(24860, 0, "m_169"),
#                                 4: Measurement(12430, 0, "m_170"),
#                                 5: Measurement(6215, 0, "m_171"),
#                             },
#                             impression=Measurement(354251, 0, "m_172"),
#                         )
#                     ],
#                     frozenset({EDP_ONE, EDP_TWO}): [
#                         MeasurementSet(
#                             reach=Measurement(13426464, 10000, "m_173"),
#                             k_reach={
#                                 1: Measurement(6929788, 10000, "m_174"),
#                                 2: Measurement(3464894, 10000, "m_175"),
#                                 3: Measurement(1732447, 10000, "m_176"),
#                                 4: Measurement(866224, 10000, "m_177"),
#                                 5: Measurement(433111, 10000, "m_178"),
#                             },
#                             impression=Measurement(26215389, 10000, "m_179"),
#                         ),
#                         MeasurementSet(
#                             reach=Measurement(3278136, 10000, "m_180"),
#                             k_reach={
#                                 1: Measurement(1691941, 10000, "m_181"),
#                                 2: Measurement(845971, 10000, "m_182"),
#                                 3: Measurement(422986, 10000, "m_183"),
#                                 4: Measurement(211493, 10000, "m_184"),
#                                 5: Measurement(105745, 10000, "m_185"),
#                             },
#                             impression=Measurement(6135862, 10000, "m_186"),
#                         )
#                     ],
#                 },
#             ),
#         "custom":
#             MetricReport(
#                 weekly_cumulative_reaches={
#                     frozenset({EDP_ONE}): [
#                         Measurement(9984642, 10000, "m_187"),
#                         Measurement(12020226, 10000, "m_188")
#                     ],
#                     frozenset({EDP_TWO}): [
#                         Measurement(5000000, 0, "m_189"),
#                         Measurement(6000000, 0, "m_190")
#                     ],
#                     frozenset({EDP_THREE}): [
#                         Measurement(800000, 0, "m_191"),
#                         Measurement(1000000, 0, "m_192")
#                     ],
#                     frozenset({EDP_ONE, EDP_TWO, EDP_THREE}): [
#                         Measurement(15799013, 10000, "m_193"),
#                         Measurement(19015392, 10000, "m_194")
#                     ],
#                 },
#                 whole_campaign_measurements=build_measurement_set(
#                     reach={
#                         frozenset({EDP_ONE}):
#                             Measurement(12017026, 10000, "m_195"),
#                         frozenset({EDP_TWO}):
#                             Measurement(6000000, 0, "m_196"),
#                         frozenset({EDP_THREE}):
#                             Measurement(1000000, 0, "m_197"),
#                         frozenset({EDP_ONE, EDP_TWO, EDP_THREE}):
#                             Measurement(19030737, 10000, "m_198"),
#                     },
#                     k_reach={
#                         frozenset({EDP_ONE}): {
#                             1: Measurement(6202336, 10000, "m_199"),
#                             2: Measurement(3101168, 10000, "m_200"),
#                             3: Measurement(1550584, 10000, "m_201"),
#                             4: Measurement(775292, 10000, "m_202"),
#                             5: Measurement(387646, 10000, "m_203"),
#                         },
#                         frozenset({EDP_TWO}): {
#                             1: Measurement(3096774, 0, "m_204"),
#                             2: Measurement(1548387, 0, "m_205"),
#                             3: Measurement(774194, 0, "m_206"),
#                             4: Measurement(387097, 0, "m_207"),
#                             5: Measurement(193548, 0, "m_208"),
#                         },
#                         frozenset({EDP_THREE}): {
#                             1: Measurement(516129, 0, "m_209"),
#                             2: Measurement(258065, 0, "m_210"),
#                             3: Measurement(129033, 0, "m_211"),
#                             4: Measurement(64517, 0, "m_212"),
#                             5: Measurement(32256, 0, "m_213"),
#                         },
#                         frozenset({EDP_ONE, EDP_TWO, EDP_THREE}): {
#                             1: Measurement(9822316, 10000, "m_214"),
#                             2: Measurement(4911158, 10000, "m_215"),
#                             3: Measurement(2455579, 10000, "m_216"),
#                             4: Measurement(1227790, 10000, "m_217"),
#                             5: Measurement(613894, 10000, "m_218"),
#                         },
#                     },
#                     impression={
#                         frozenset({EDP_ONE}):
#                             Measurement(22871159, 10000, "m_219"),
#                         frozenset({EDP_TWO}):
#                             Measurement(11216125, 0, "m_220"),
#                         frozenset({EDP_THREE}):
#                             Measurement(1844136, 0, "m_221"),
#                         frozenset({EDP_ONE, EDP_TWO, EDP_THREE}):
#                             Measurement(35936915, 10000, "m_222"),
#                     }),
#                 weekly_non_cumulative_measurements={
#                     frozenset({EDP_ONE}): [
#                         MeasurementSet(
#                             reach=Measurement(10000981, 10000, "m_223"),
#                             k_reach={
#                                 1: Measurement(5161797, 10000, "m_224"),
#                                 2: Measurement(2580899, 10000, "m_225"),
#                                 3: Measurement(1290450, 10000, "m_226"),
#                                 4: Measurement(645225, 10000, "m_227"),
#                                 5: Measurement(322610, 10000, "m_228"),
#                             },
#                             impression=Measurement(18382797, 10000, "m_229"),
#                         ),
#                         MeasurementSet(
#                             reach=Measurement(2441042, 10000, "m_230"),
#                             k_reach={
#                                 1: Measurement(1259893, 10000, "m_231"),
#                                 2: Measurement(629947, 10000, "m_232"),
#                                 3: Measurement(314974, 10000, "m_233"),
#                                 4: Measurement(157487, 10000, "m_234"),
#                                 5: Measurement(78741, 10000, "m_235"),
#                             },
#                             impression=Measurement(4488362, 10000, "m_236"),
#                         )
#                     ],
#                     frozenset({EDP_TWO}): [
#                         MeasurementSet(
#                             reach=Measurement(5000000, 0, "m_237"),
#                             k_reach={
#                                 1: Measurement(2580645, 0, "m_238"),
#                                 2: Measurement(1290323, 0, "m_239"),
#                                 3: Measurement(645162, 0, "m_240"),
#                                 4: Measurement(322581, 0, "m_241"),
#                                 5: Measurement(161289, 0, "m_242"),
#                             },
#                             impression=Measurement(9193546, 0, "m_243"),
#                         ),
#                         MeasurementSet(
#                             reach=Measurement(1100000, 0, "m_244"),
#                             k_reach={
#                                 1: Measurement(567742, 0, "m_245"),
#                                 2: Measurement(283871, 0, "m_246"),
#                                 3: Measurement(141936, 0, "m_247"),
#                                 4: Measurement(70968, 0, "m_248"),
#                                 5: Measurement(35483, 0, "m_249"),
#                             },
#                             impression=Measurement(2022579, 0, "m_250"),
#                         )
#                     ],
#                     frozenset({EDP_THREE}): [
#                         MeasurementSet(
#                             reach=Measurement(800000, 0, "m_251"),
#                             k_reach={
#                                 1: Measurement(412903, 0, "m_252"),
#                                 2: Measurement(206452, 0, "m_253"),
#                                 3: Measurement(103226, 0, "m_254"),
#                                 4: Measurement(51613, 0, "m_255"),
#                                 5: Measurement(25806, 0, "m_256"),
#                             },
#                             impression=Measurement(1470967, 0, "m_257"),
#                         ),
#                         MeasurementSet(
#                             reach=Measurement(202952, 0, "m_258"),
#                             k_reach={
#                                 1: Measurement(104749, 0, "m_259"),
#                                 2: Measurement(52375, 0, "m_260"),
#                                 3: Measurement(26188, 0, "m_261"),
#                                 4: Measurement(13094, 0, "m_262"),
#                                 5: Measurement(6546, 0, "m_263"),
#                             },
#                             impression=Measurement(373169, 0, "m_264"),
#                         )
#                     ],
#                     frozenset({EDP_ONE, EDP_TWO, EDP_THREE}): [
#                         MeasurementSet(
#                             reach=Measurement(15819974, 10000, "m_265"),
#                             k_reach={
#                                 1: Measurement(8165148, 10000, "m_266"),
#                                 2: Measurement(4082574, 10000, "m_267"),
#                                 3: Measurement(2041287, 10000, "m_268"),
#                                 4: Measurement(1020644, 10000, "m_269"),
#                                 5: Measurement(510321, 10000, "m_270"),
#                             },
#                             impression=Measurement(29052805, 10000, "m_271"),
#                         ),
#                         MeasurementSet(
#                             reach=Measurement(3751542, 10000, "m_272"),
#                             k_reach={
#                                 1: Measurement(1936280, 10000, "m_273"),
#                                 2: Measurement(968140, 10000, "m_274"),
#                                 3: Measurement(484070, 10000, "m_275"),
#                                 4: Measurement(242035, 10000, "m_276"),
#                                 5: Measurement(121017, 10000, "m_277"),
#                             },
#                             impression=Measurement(6884110, 10000, "m_278"),
#                         )
#                     ],
#                 },
#             )
#     },
#     metric_subsets_by_parent={"ami": ["mrc", "custom"]},
#     cumulative_inconsistency_allowed_edp_combinations={}
# )


class TestReportMcApi2(unittest.TestCase):
    def aways_true(self):
        return self.assertEqual(1,1)



if __name__ == "__main__":
    unittest.main()
