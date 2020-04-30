import com.typesafe.config.ConfigFactory
import mark.zakharov.StyleTransferBot

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main extends App {
  override def main(args: Array[String]): Unit = {
    val bot = new StyleTransferBot(ConfigFactory.load().getString("styletransferbot.token"))
    val eol = bot.run()
    println("Press [ENTER] to shutdown the bot, it may take a few seconds...")
    scala.io.StdIn.readLine()
    bot.shutdown() // initiate shutdown
    // Wait for the bot end-of-life
    Await.result(eol, Duration.Inf)
  }
}

