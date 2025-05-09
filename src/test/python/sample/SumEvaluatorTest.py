import unittest
from src.main.python.sample.SumEvaluator import SumEvaluator


class TestSumEvaluator(unittest.TestCase):

    def test_evaluate_sum(self):
        self.assertEqual(SumEvaluator.evaluate(1, 2), 3,
                         "when a = 1 and b = 2, then returns  3")


if __name__ == '__main__':
    unittest.main()
