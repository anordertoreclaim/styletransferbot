package mark.zakharov

import com.softwaremill.sttp.okhttp.OkHttpFutureBackend
import cats.instances.future._
import cats.syntax.functor._
import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.api.declarative.{Commands, InlineQueries}
import com.bot4s.telegram.clients.FutureSttpClient
import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.bot4s.telegram.methods.SendPhoto
import com.bot4s.telegram.models.InputFile
import slogging.{LogLevel, LoggerConfig, PrintLoggerFactory}
import com.bot4s.telegram.Implicits._
import com.bot4s.telegram.api.declarative._
import com.bot4s.telegram.api.ChatActions
import com.bot4s.telegram.models._
import io.circe._
import io.circe.parser._

import scala.concurrent.Future

class StyleTransferBot(val token: String) extends TelegramBot
  with ChatActions[Future]
  with Commands[Future]
  with InlineQueries[Future]
  with Polling {
  LoggerConfig.factory = PrintLoggerFactory()
  // set log level, e.g. to TRACE
  LoggerConfig.level = LogLevel.TRACE

  implicit val backend = OkHttpFutureBackend()
  override val client: RequestHandler[Future] = new FutureSttpClient(token)

  onCommand("start") { implicit msg =>
    reply( "Send me a picture and I will apply style transfer algorithm to it!").void
  }

  def nonEmptyImage(message: Message): Boolean = message.photo.isDefined

  whenOrElse(onMessage, nonEmptyImage) {
    implicit msg =>
      val photo = msg.photo.get(1)
      for {
        req <- Future { scalaj.http.Http(s"https://api.telegram.org/bot$token/getFile?file_id=${photo.fileId}").asString }
        if req.isDefined
        json = parse(req.body).getOrElse(Json.Null)
        filePath: Decoder.Result[String] = json.hcursor.downField("result").downField("file_path").as[String]
        if filePath.isRight
        r <- Future { scalaj.http.Http(s"https://api.telegram.org/file/bot$token/${filePath.right.get}").asBytes }
        bytes = r.body
        transformedBytes = StyleTransfer.performOnBytes(bytes)
        _ <- uploadingPhoto // hint the user
        outputFile = InputFile("transformed_image.png", transformedBytes)
        _ <- request(SendPhoto(msg.source, outputFile))
      } yield ()
  } {
    implicit msg =>
      unit
  }
}