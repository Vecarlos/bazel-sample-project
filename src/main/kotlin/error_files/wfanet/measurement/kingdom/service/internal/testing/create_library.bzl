load("@wfa_rules_kotlin_jvm//kotlin:defs.bzl", "kt_jvm_library")

def create_library(name, deps):
    """Macro para crear una kt_jvm_library para un solo archivo de prueba."""
    kt_jvm_library(
        name = name,
        testonly = True,
        srcs = [name + ".kt"],
        deps = deps,
    )
