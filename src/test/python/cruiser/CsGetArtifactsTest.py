import unittest
import cruiser.CsGetArtifacts as cs_get_artifacts
from unittest.mock import patch, mock_open, MagicMock
import json


class TestCsGetArtifacts(unittest.TestCase):

    def test_get_artifact(self):
        fake_artifact = 'source:artifact:version'
        artifact = cs_get_artifacts.get_artifact(fake_artifact)
        expected_artifact = 'source:artifact'
        self.assertEqual(artifact, expected_artifact)

    @patch('builtins.open', new_callable=mock_open)
    def test_get_json_artifacts(self, mock_file_open):
        fake_content = {
            "artifacts": {
                "group:artifact_1": {"version": "1.0"},
                "group:artifact_2": {"version": "2.0"}
            }
        }
        fake_json_string = json.dumps(fake_content)
        mock_file_open.return_value = mock_open(
            read_data=fake_json_string).return_value
        mocked_path = "path"
        key, data = cs_get_artifacts.get_json_artifacts(mocked_path)
        expected_keys = {"group:artifact_1", "group:artifact_2"}
        expected_data = fake_content["artifacts"]
        self.assertEqual(key, expected_keys)
        self.assertEqual(data, expected_data)

    @patch("cruiser.CsGetArtifacts.get_artifact")
    @patch("cruiser.CsGetArtifacts.get_json_artifacts")
    def test_get_total_artifacts(self, mock_get_json_artifacts, mock_get_artifact):
        cs_get_artifacts.ARTIFACTS_WITH_VERSION = [
            "group:artifact_1:1.0", "group:artifact_2:2.0"]
        mock_get_artifact.side_effect = [
            "group:artifact_1", "group:artifact_2"]
        mock_get_json_artifacts.return_value = (
            {"group:artifact_1", "group:artifact_3"},
            {"group:artifact_2": {"version": "2.0"},
                "group:artifact_3": {"version": "3.0"}}
        )

        result = cs_get_artifacts.get_total_artifacts("fake/path")
        expected = [
            "group:artifact_1:1.0",
            "group:artifact_2:2.0",
            "group:artifact_3:3.0"
        ]
        self.assertEqual(result, expected)

    def test_get_command(self):
        fake_total_artifacts = ["group:artifact_1:1.0"]
        command = cs_get_artifacts.get_command(fake_total_artifacts)
        expected_command = 'cs resolve\\\n\tgroup:artifact_1:1.0 \\\n\t--tree \\\n\t-r central \\\n\t-r "https://maven.google.com" \\\n\t-r "https://packages.confluent.io/maven/"'
        self.assertEqual(command, expected_command)

    @patch("subprocess.run")
    @patch("cruiser.CsGetArtifacts.get_total_artifacts")
    @patch("cruiser.CsGetArtifacts.get_command")
    def test_run_cruiser_command(self, mock_get_command, mock_get_total_artifacts, mock_subprocess_run):
        mock_get_command.return_value = 'command'
        mock_get_total_artifacts.return_value = []
        cs_get_artifacts.run_cruiser_command()
        mock_subprocess_run.assert_called_once_with('command', shell=True)

    @patch("cruiser.CsGetArtifacts.run_cruiser_command")
    def test_init(self, mock_run_cruiser_command):
        with patch.object(cs_get_artifacts, "__name__", "__main__"):
            cs_get_artifacts.main()
        mock_run_cruiser_command.assert_called_once()


if __name__ == '__main__':
    unittest.main()
