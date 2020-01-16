package utilities
import scopt._

case class Config(
                   url: String = "",
                   maxProcess: Int = 5096
                 )

object ArgParser {
 val builder: OParserBuilder[Config] = OParser.builder[Config]

 val parser: OParser[Unit, Config] = {
  import builder._
  OParser.sequence(
   programName("HULK"),
   head("HULK Unbearable Load King"),
   opt[String]('u', "url")
       .action((x, c) => c.copy(url = x))
       .required(),
   opt[Int]('m', "max-process")
     .action((x, c) => c.copy(maxProcess = x)),
   help("help")
  )
 }
}
