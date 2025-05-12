import unittest
from sample.ManageUsers import create_new_user
from src.main.proto.sample import user_profile_pb2

class TestManageUsers(unittest.TestCase):

    def test_create_user_profile(self):
        user_id: str = "py_test_001"
        name: str = "Python Test User Proto"
        age: int = 28
        is_active: bool = True
        interests: list[str] = ["testing", "python", "protobuf"]

        profile = create_new_user(user_id, name, age, is_active, interests)

        self.assertIsInstance(profile, user_profile_pb2.UserProfile)
        self.assertEqual(profile.user_id, user_id)
        self.assertEqual(profile.display_name, name)
        self.assertEqual(profile.age, age)
        self.assertEqual(profile.is_active, is_active)
        self.assertListEqual(list(profile.interests), interests)

if __name__ == '__main__':
    unittest.main()