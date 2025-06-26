#reference https://github.com/world-federation-of-advertisers/cross-media-measurement/blob/main/src/main/k8s/macros.bzl
load("@wfa_rules_cue//cue:defs.bzl", "cue_export")

def cue_dump(name, srcs = None, deps = None, cue_tags = None, **kwargs):
    cue_export(
        name = name,
        srcs = srcs,
        deps = deps,
        filetype = "yaml",
        expression = "listObject",
        cue_tags = cue_tags,
        **kwargs
    )