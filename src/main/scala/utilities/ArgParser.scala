package utilities

import scopt.OParser
case class Config(
                   url: String = "http://localhost/",
                   maxProcess: Int = 512
                 )

object ArgParser {
 val builder = OParser.builder[Config]

 val parser: OParser[Unit, Config] = {
  import builder._
  OParser.sequence(
   programName("HULK"),
   head("HULK Unbearable Load King"),
   opt[String]('u', "url")
       .action((x, c) => c.copy(url = x)),
   opt[Int]('m', "max-process")
     .action((x, c) => c.copy(maxProcess = x)),
   help("help")
  )
 }
}
