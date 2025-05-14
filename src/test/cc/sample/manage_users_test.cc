#include "src/main/cc/sample/manage_users.h"
#include "src/main/proto/sample/user_profile.pb.h"
#include "gtest/gtest.h"
#include <string>
#include <vector>
#include <algorithm>

namespace sample
{
    namespace
    {

        TEST(ManageUsersTest, CreateUserProfilePopulatesFieldsCorrectly)
        {
            std::string user_id = "cpp_test_001";
            std::string display_name = "C++ Test User Proto";
            int32_t age = 28;
            bool is_active = true;
            std::vector<std::string> interests = {"testing", "cpp", "protobuf"};

            UserProfile profile = createNewUser(user_id, display_name, age, is_active, interests);

            EXPECT_EQ(profile.user_id(), user_id);
            EXPECT_EQ(profile.display_name(), display_name);
            EXPECT_EQ(profile.age(), age);
            EXPECT_EQ(profile.is_active(), is_active);

            ASSERT_EQ(profile.interests_size(), interests.size());
            for (size_t i = 0; i < interests.size(); ++i)
            {
                EXPECT_EQ(profile.interests(static_cast<int>(i)), interests[i]);
            }
        }

    }
}