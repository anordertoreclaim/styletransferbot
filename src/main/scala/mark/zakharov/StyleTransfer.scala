package mark.zakharov

import java.io.{BufferedInputStream, File, FileInputStream}

import org.bytedeco.javacpp.opencv_core.{Mat, Size}
import org.bytedeco.javacpp.opencv_imgproc.resize
import org.platanios.tensorflow.api.core.Graph
import org.platanios.tensorflow.api.core.client.Session
import org.tensorflow.framework.GraphDef
import org.platanios.tensorflow.api.Tensor

object StyleTransfer {
  private val graph = Graph.fromGraphDef(GraphDef.parseFrom(
    new BufferedInputStream(new FileInputStream(new File("model/style_transfer.pb")))))

  def perform(image: Mat): Mat = {
    val imageResized = new Mat
    val prevWidth = image.arrayWidth()
    val prevHeight = image.arrayHeight()
    resize(image, imageResized, new Size(300, 300))
    val imageTensor = CVUtils.imageToTensor(imageResized)
    val transformedTensor = runStyleTransfer(imageTensor)
    val transformedImage = CVUtils.tensorToImage(transformedTensor)
    val transformedImageResized = new Mat
    resize(transformedImage, transformedImageResized, new Size(prevWidth, prevHeight))
    transformedImageResized
  }

  def performOnBytes(imageBytes: Array[Byte]): Array[Byte] = {
    val image = CVUtils.byteArrayToImage(imageBytes)
    val transformedImage = perform(image)
    val transformedBytes = CVUtils.imageToByteArray(transformedImage)
    transformedBytes
  }

  private def runStyleTransfer(imageTensor: Tensor): Tensor = {
    val inputPlaceholder = graph.getOutputByName("image_placeholder:0")
    val outputPlaceholder = graph.getOutputByName("Cast_1:0")
    val feeds = Map(inputPlaceholder -> imageTensor)
    Session(graph).run(fetches = outputPlaceholder, feeds = feeds)
  }
}
