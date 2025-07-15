package replicate_error

import org.junit.Before
import org.junit.Test
import replicate_error.ApiKeysServiceTest

abstract class ApiKeysServiceTest<T : Int> {

//   protected lateinit var services: Services<T>

//   protected data class Services<T>(
//     val apiKeysService: T
//   )

//   abstract fun newServices(idGenerator: IdGenerator): Services<T>

  @Before
  fun initServices() {
  }

  @Test
  fun `createApiKey with no description returns an api key`() {
  }

  @Test
  fun `createApiKey with description returns an api key`() {}

  @Test
  fun `createApiKey throws NOT_FOUND when measurement consumer doesn't exist`() {}

  @Test
  fun `deleteApiKey returns the api key`() {}

  @Test
  fun `deleteApiKey throws NOT_FOUND when measurement consumer doesn't exist`() {}

  @Test
  fun `deleteApiKey throws NOT FOUND when api key doesn't exist`() {}

  @Test
  fun `authenticateApiKey returns a measurement consumer`() {}

  @Test
  fun `authenticateApiKey throws INVALID_ARGUMENT when authentication key hash is missing`() {}

  @Test
  fun `authenticateApiKey throws NOT_FOUND when authentication key hash doesn't match`() {}

  @Test
  fun `authenticateApiKey throws NOT_FOUND when api key has been deleted`() {}
}
