#reference https://github.com/world-federation-of-advertisers/cross-media-measurement/blob/main/src/main/docker/macros.bzl
load(
    "@wfa_common_jvm//build/rules_oci:defs.bzl",
    _java_image = "java_image",
)

def java_image(
        name,
        binary,
        # buildifier: disable=unused-variable
        main_class = None,
        args = None,
        base = None,
        tags = None,
        visibility = None,
        **kwargs):
    """Java container image.

    This is a replacement for the java_image rule which sets common attrs.
    """
    tags = tags or []

    _java_image(
        name = name,
        binary = binary,
        base = base,
        labels = {},
        cmd_args = args,
        tags = tags + ["no-remote-cache"],
        visibility = visibility,
        **kwargs
    )