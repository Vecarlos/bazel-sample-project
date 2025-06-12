package issue

fun isValidHexColor(color: String): Boolean {
    val hexPattern = Regex("#[0-9a-fA-f]{6}") // <--- VULNERABILIDAD AQUÍ

    return color.matches(hexPattern)
}

fun main() {
    val validColor = "#1a2B3c"
    val invalidColorButMatches = "#ABC[DE" 

    println("${isValidHexColor(validColor)}")
    println("${isValidHexColor(invalidColorButMatches)}")
}