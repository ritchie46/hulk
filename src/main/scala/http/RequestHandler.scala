package http
import java.net.Proxy
import scalaj.http._

class RequestHandler(
            proxyConfig: Option[Proxy] = None,
            options: Seq[HttpOptions.HttpOption] = HttpConstants.defaultOptions,
            charset: String = HttpConstants.utf8,
            sendBufferSize: Int = 4096,
            compress: Boolean = true
          ) extends BaseHttp (
  proxyConfig = None,

) {

  override def apply(url: String): HttpRequest = HttpRequest(
    url = url,
    method = "GET",
    connectFunc = DefaultConnectFunc,
    params = Nil,
    headers = Seq(),
    options = options,
    proxyConfig = proxyConfig,
    charset = charset,
    sendBufferSize = sendBufferSize,
    urlBuilder = QueryStringUrlFunc,
    compress = compress,
    digestCreds = None
  )
}
