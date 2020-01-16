package http
import scalaj.http.HttpResponse
import utilities._

import scala.util.{Random, Try}

class HttpCaller(val url: String, val headers: Headers) {
  val paramJoiner: String = if (url.contains("?")) "&" else "?"
  val handler = new RequestHandler()

  def uniqueUrl: String =
    s"${url}${paramJoiner}${buildBlock(Random.between(3, 10))}=${buildBlock(Random.between(3, 10))}"

  def apply(): Try[HttpResponse[Array[Byte]]] = Try(handler
    .apply(uniqueUrl)
    // Standard values led to SocketTimeoutException
    .timeout(connTimeoutMs = 5000, readTimeoutMs = 5000)
    .headers(headers.generate).asBytes)
}