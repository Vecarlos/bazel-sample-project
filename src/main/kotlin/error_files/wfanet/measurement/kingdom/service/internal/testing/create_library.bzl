load("@wfa_rules_kotlin_jvm//kotlin:defs.bzl", "kt_jvm_library")

def create_library(name, deps):
    kt_jvm_library(
        name = name,
        testonly = True,
        srcs = [name + ".kt"],
        deps = deps,
    )
