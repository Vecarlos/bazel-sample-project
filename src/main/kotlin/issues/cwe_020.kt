package issues

// Simula una función que obtiene un patrón de una fuente externa.
fun getPatternFromUnsafeSource(): String {
    // VULNERABILIDAD: El rango [A-f] es demasiado amplio.
    return "#[0-9a-fA-f]{6}"
}

fun main() {
    val validColor = "#1a2B3c"
    val invalidColorButMatches = "#ABC[DE" 
    
    // CAMBIO CLAVE: El patrón vulnerable ahora viene de una "fuente".
    val hexPattern = Regex(getPatternFromUnsafeSource())

    fun isValidHexColor(color: String): Boolean {
        // El "sink" vulnerable, donde se usa el patrón.
        return color.matches(hexPattern)
    }

    println("Probando color válido: ${isValidHexColor(validColor)}")
    println("Probando color inválido: ${isValidHexColor(invalidColorButMatches)}")
}