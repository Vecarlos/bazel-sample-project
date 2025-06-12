#include <iostream>

enum privileges {
    NONE,
    PARTIAL,
    FULL
};

void restrict_privileges(privileges p) {
    if (p != FULL) {
        std::cout << "No all Acess." << std::endl;
    } else {
        std::cout << "Acess." << std::endl;
    }
}

int main() {
    bool is_admin = false;
    enum privileges entitlements = NONE;

    if (is_admin)
        entitlements = FULL, 
    restrict_privileges(entitlements);

    std::cout << "End." << std::endl;
    return 0;
}