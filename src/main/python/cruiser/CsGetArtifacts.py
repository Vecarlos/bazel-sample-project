import json
import subprocess

ARTIFACTS_WITH_VERSION = [
    "com.google.crypto.tink:tink-awskms:1.9.1",
    "com.google.crypto.tink:tink-gcpkms:1.9.0",
    "com.squareup.okhttp3:okhttp:4.12.0",
    "io.opentelemetry.semconv:opentelemetry-semconv:1.22.0-alpha",
    "io.kubernetes:client-java:21.0.1",
    "io.kubernetes:client-java-extended:21.0.1",
    "joda-time:joda-time:2.10.10",
    "org.slf4j:slf4j-simple:1.7.32",
    "com.google.apis:google-api-services-storage:v1-rev20240706-2.0.0",
    "org.bouncycastle:bcpkix-jdk18on:1.79",
    "junit:junit:4.13.2",
    "org.conscrypt:conscrypt-openjdk-uber:2.5.2",
    "org.jetbrains:annotations:23.0.0",
    "info.picocli:picocli:4.7.6",
    "com.google.code.gson:gson:2.10.1",
    "io.netty:netty-handler:4.1.108.Final",
    "com.google.crypto.tink:tink:1.12.0",
    "com.github.ben-manes.caffeine:caffeine:3.1.8",
    "com.google.apis:google-api-services-bigquery:v2-rev20240815-2.0.0",
    "com.google.cloud.functions:functions-framework-api:1.1.0",
    "com.google.cloud:google-cloudevent-types:0.16.0",
    "com.google.cloud.sql:postgres-socket-factory:1.12.0",
    "com.google.cloud.sql:cloud-sql-connector-r2dbc-core:1.12.0",
    "com.google.cloud.sql:cloud-sql-connector-r2dbc-postgres:1.12.0",
    "org.postgresql:postgresql:42.7.0",
    "org.postgresql:r2dbc-postgresql:1.0.4.RELEASE",
    "io.r2dbc:r2dbc-spi:1.0.0.RELEASE",
    "org.yaml:snakeyaml:2.2",
    "org.liquibase:liquibase-core:4.26.0",
    "com.google.cloudspannerecosystem:liquibase-spanner:4.25.1",
    "org.liquibase.ext:liquibase-postgresql:4.11.0",
    "org.apache.commons:commons-math3:3.6.1",
    "org.apache.commons:commons-numbers-gamma:1.1",
    "com.opencsv:opencsv:5.6",
    "org.apache.commons:commons-compress:1.22",
    "org.brotli:dec:0.1.2",
    "com.github.luben:zstd-jni:1.5.2-5",
    "org.mockito:mockito-core:5.12.0",
    "org.mockito.kotlin:mockito-kotlin:5.4.0",
    "com.google.truth:truth:1.4.4",
    "com.google.truth.extensions:truth-java8-extension:1.4.4",
    "com.google.truth.extensions:truth-proto-extension:1.4.4",
]

CRUISER_COMMAND = "cs resolve"
TREE_DISPLAY = '\n\t--tree'
MAVEN_REPOSITORIES = '\\\n\t-r central \\\n\t-r "https://maven.google.com" \\\n\t-r "https://packages.confluent.io/maven/"'
PATH_MAVEN_INSTALL = './maven_install.json'
CRUISER_COMMAND_PATH = './crousier_comand.sh'


def get_artifact(row):
    row_splitted = row.split(':')
    artifact = ':'.join(row_splitted[:-1])
    return artifact


def get_json_artifacts(json_path):
    with open(json_path, 'r') as file:
        data = json.load(file)
    return set(data['artifacts'].keys()), data['artifacts']


def get_total_artifacts(path_maven_install):
    artifacts_without_version = [get_artifact(
        artifact_with_version) for artifact_with_version in ARTIFACTS_WITH_VERSION]
    total_artifacts = [*ARTIFACTS_WITH_VERSION]
    artifacts_name, artifacts_data = get_json_artifacts(path_maven_install)
    for artifact_name in artifacts_name:
        if artifact_name not in artifacts_without_version:
            total_artifacts.append(artifact_name+':'+artifacts_data[artifact_name]['version'])
    return total_artifacts


def get_command(total_artifacts):
    comand = CRUISER_COMMAND + "\\"
    for artifact in total_artifacts:
        comand += '\n\t' + artifact + " \\"
    comand += TREE_DISPLAY + ' ' + MAVEN_REPOSITORIES
    return comand


def run_cruiser_command():
    total_artifacts = get_total_artifacts(PATH_MAVEN_INSTALL)
    command = get_command(total_artifacts)
    subprocess.run(command, shell=True)


def main():
    if __name__ == "__main__":
        run_cruiser_command()


main()
