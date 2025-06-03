from src.main.proto.sample import user_profile_pb2


def create_new_user(user_id: str, name: str, age: int, is_active: bool, interests: list[str]) -> user_profile_pb2.UserProfile:
    profile = user_profile_pb2.UserProfile()
    profile.user_id = user_id
    profile.display_name = name
    profile.age = age
    profile.is_active = is_active
    profile.interests.extend(interests)
    return profile
