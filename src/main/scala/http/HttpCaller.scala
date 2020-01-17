package http
import scalaj.http.HttpResponse
import utilities._

import scala.util.{Random, Try}

class HttpCaller(val url: String,
                 val headers: Headers,
                 val timeout: Int) {
  val paramJoiner: String = if (url.contains("?")) "&" else "?"
  val handler = new RequestHandler()

  def uniqueUrl: String =
    s"${url}${paramJoiner}${buildBlock(Random.between(3, 10))}=${buildBlock(Random.between(3, 10))}"

  def apply(): Try[HttpResponse[Array[Byte]]] = Try(handler
    .apply(uniqueUrl)
    .timeout(connTimeoutMs = timeout, readTimeoutMs = timeout)
    .headers(headers.generate).asBytes)
}