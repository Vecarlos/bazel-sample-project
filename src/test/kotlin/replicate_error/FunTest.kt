
package replicate_error

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Esta es la clase que Bazel ejecutará.
 * 1. Es una clase normal (no abstracta).
 * 2. Tiene la anotación @RunWith para que JUnit la reconozca.
 */
@RunWith(JUnit4::class)
class FunTest {

    @Test
    fun `mi test vacio que funciona`() {
        // Este test está vacío y ahora sí se ejecutará correctamente.
        assert(true)
    }
}