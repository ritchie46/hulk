package utilities
import scopt._

case class Config(
                   url: String = "",
                   maxProcess: Int = 2048,
                   timeout: Int = 5000,
                 )

object ArgParser {
 val builder: OParserBuilder[Config] = OParser.builder[Config]

 val parser: OParser[Unit, Config] = {
  import builder._
  OParser.sequence(
   programName("HULK"),
   head("HULK: Http Unbearable Load King"),
   opt[String]('u', "url")
       .action((x, c) => c.copy(url = x))
       .required(),
   opt[Int]('m', "max-process")
     .action((x, c) => c.copy(maxProcess = x))
       .text("Upper parallel requests limit"),
   opt[Int]('t', "timeout")
      .action((x, c) => c.copy(timeout = x))
       .text("Connection timeout duration in ms"),
   help("help")
  )
 }
}
