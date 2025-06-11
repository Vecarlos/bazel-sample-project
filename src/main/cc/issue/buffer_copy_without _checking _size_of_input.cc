#include <iostream>
#include <cstring> // Para usar strcpy

void processUserData(char* input) {
    char user_buffer[50];


    // ISSUE: strcpy no comprueba el tamaño del búfer de destino.
    // Si 'input' tiene más de 49 caracteres (+1 para el terminador nulo '\0'),
    // se escribirá fuera de los límites de 'user_buffer', corrompiendo la memoria.
    strcpy(user_buffer, input); // <--- ALERTA DE SEGURIDAD AQUÍ

}

int main(int argc, char* argv[]) {
    if (argc > 1) {
        processUserData(argv[1]);
    } else {
        char safe_input[] = "Vecarlos";
        processUserData(safe_input);

        char unsafe_input[] = "EsteEsUnNombreDeUsuarioExtremadamenteLargoQueDefinitivamenteNoCabeEnElBufferDeCincuentaBytes";
        processUserData(unsafe_input);
    }

    return 0;
}