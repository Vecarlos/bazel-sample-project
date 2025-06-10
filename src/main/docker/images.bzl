COMMON_IMAGES = [
    struct(
        name = "load_count_letters_server_image",
        image = "//src/main/kotlin/com:count_letters_server_docker_image",
        repository = "count-letters-server",
    ),
    struct(
        name = "load_count_letters_client_image",
        image = "//src/main/kotlin/client:count_letters_client_docker_image",
        repository = "count-letters-server",
    ),
]
