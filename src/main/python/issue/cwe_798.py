import hashlib


def hash_password(password):

    hashed_password = hashlib.md5(password.encode('utf-8')).hexdigest()

    return hashed_password


def check_password(password, stored_hash):
    return hash_password(password) == stored_hash
