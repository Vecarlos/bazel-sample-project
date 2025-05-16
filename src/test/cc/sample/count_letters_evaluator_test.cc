#include "sample/count_letters_evaluator.h"
#include "gtest/gtest.h"

namespace sample
{
    namespace
    {

        TEST(CountLettersEvaluatorTest, HandlesEmptyString)
        {
            EXPECT_EQ(CountLettersEvaluator::evaluate(""), 0);
        }

        TEST(CountLettersEvaluatorTest, HandlesNonEmptyString)
        {
            EXPECT_EQ(CountLettersEvaluator::evaluate("hello"), 5);
        }

    }
}