#include "src/main/cc/sample/manage_users.h"

namespace sample
{

  sample::UserProfile createNewUser(const std::string &user_id,
                                    const std::string &display_name,
                                    int32_t age,
                                    bool is_active,
                                    const std::vector<std::string> &interests)
  {
    sample::UserProfile profile;
    profile.set_user_id(user_id);
    profile.set_display_name(display_name);
    profile.set_age(age);
    profile.set_is_active(is_active);
    for (const std::string &interest : interests)
    {
      profile.add_interests(interest);
    }
    return profile;
  }

}