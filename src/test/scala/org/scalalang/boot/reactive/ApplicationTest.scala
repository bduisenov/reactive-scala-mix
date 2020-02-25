package org.scalalang.boot.reactive

import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.{AfterAll, BeforeAll, Test, TestInstance}
import org.scalalang.boot.reactive.Application._
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters

@TestInstance(Lifecycle.PER_CLASS)
class ApplicationTest {

  val client: WebTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:8080").build()

  var context: ConfigurableApplicationContext = _

  @BeforeAll
  def beforeAll(): Unit = {
    context = app.run()
  }

  @Test
  def whenAnInvalidIdIsGivenThenReturnsBadRequest(): Unit = {
    client.get()
      .uri("/1")
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  def whenAValidJsonIsGivenThenReturnsTheIdOfACreatedResource(): Unit = {
    val json = """{ "name": "Monika" }"""

    client.post().uri("/")
      .contentType(APPLICATION_JSON)
      .body(BodyInserters.fromValue(json))
      .exchange()
      .expectStatus().is2xxSuccessful
  }

  @AfterAll
  def afterAll(): Unit = {
    context.close()
  }
}
