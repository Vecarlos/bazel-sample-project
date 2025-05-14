#ifndef SRC_MAIN_CPP_SAMPLE_MANAGE_USERS_H_
#define SRC_MAIN_CPP_SAMPLE_MANAGE_USERS_H_

#include <string>
#include <vector>
#include "src/main/proto/sample/user_profile.pb.h"

namespace sample
{

    sample::UserProfile createNewUser(const std::string &user_id,
                                      const std::string &display_name,
                                      int32_t age,
                                      bool is_active,
                                      const std::vector<std::string> &interests);

}

#endif