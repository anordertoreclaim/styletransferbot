package mark.zakharov

import java.nio.ByteBuffer

import org.bytedeco.javacpp.BytePointer
import org.bytedeco.javacpp.indexer.UByteRawIndexer
import org.bytedeco.javacpp.opencv_core.{CV_8UC3, Mat}
import org.bytedeco.javacpp.opencv_imgproc.{COLOR_BGR2RGB, cvtColor}
import org.platanios.tensorflow.api.{Shape, Tensor, UINT8}
import org.bytedeco.javacpp.opencv_imgcodecs.{CV_LOAD_IMAGE_UNCHANGED, imdecode, imencode}

object CVUtils {
  def imageToTensor(image: Mat): Tensor = {
    val imgRGB = image
    cvtColor(image, imgRGB, COLOR_BGR2RGB) // convert channels from OpenCV BGR to RGB
    val imgBuffer = imgRGB.createBuffer[ByteBuffer]
    val height = image.rows()
    val width = image.cols()
    val shape = Shape(1, height, width, 3)
    Tensor.fromBuffer(UINT8, shape, imgBuffer.capacity, imgBuffer)
  }

  def tensorToImage(tensor: Tensor): Mat = {
    val height = tensor.shape.size(1)
    val width = tensor.shape.size(2)
    val imgRGB = new Mat(height, width, CV_8UC3)
    val iterator = tensor.entriesIterator
    val indexer: UByteRawIndexer = imgRGB.createIndexer()
    for (y <- 0 until height) {
      for (x <- 0 until width) {
        // RGB -> BGR
        for (c <- Seq(2, 1, 0)) {
          val stringRepr = iterator.next().toString
          val intRepr = stringRepr.split("\\.")(0).toInt
          indexer.put(Array(y: Long, x, c), intRepr)
        }
      }
    }
    imgRGB
  }

  def byteArrayToImage(bytes: Array[Byte]): Mat = {
    imdecode(new Mat(bytes, true), CV_LOAD_IMAGE_UNCHANGED)
  }

  def imageToByteArray(mat: Mat): Array[Byte] = {
    val bytePointer = new BytePointer()
    imencode(".png", mat, bytePointer)
    bytePointer.getStringBytes
  }
}
