#include "src/main/cc/sample/sum_evaluator.h"
#include "gtest/gtest.h"

namespace sample
{
    namespace
    {

        TEST(SumEvaluatorTest, AddsPositiveNumbers)
        {
            EXPECT_EQ(SumEvaluator::evaluate(1, 2), 3);
        }

    }
}