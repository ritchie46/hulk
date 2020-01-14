package http
import scala.util.Random
import utilities._

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