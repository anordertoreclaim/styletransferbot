package mark.zakharov

import org.scalatest.{FlatSpec, Matchers}
import CVUtils._
import org.bytedeco.javacpp.opencv_imgcodecs.{imread, imwrite}
import org.bytedeco.javacpp.opencv_core.CV_8UC3
import org.platanios.tensorflow.api.{Shape, Tensor, UINT8}

class CVUtilsSpec extends FlatSpec with Matchers {
  "imageToTensor(tensorToImage(tensor))" should "preserve tensor" in {
    val shape = Shape(Array(1, 300, 300, 3))
    // I cannot create a tensor of type UINT8 with random values with this library :(
    val tensor = Tensor.zeros(UINT8, shape=shape)
    val transformedTensor = imageToTensor(tensorToImage(tensor))
    val tensorElements = tensor.entriesIterator.toSeq
    val transformedTensorElements = transformedTensor.entriesIterator.toSeq
    tensorElements shouldEqual transformedTensorElements
  }

  "tensorToImage(imageToTensor(image))" should "preserve image" in {
    val shrekImage = imread("src/test/test_images/shrek.jpg")
    // I did not manage to find a way to compare Mat instances elementwise, so I will simply
    // save the image back for you to decide whether the image is preserved or not
    val shrekFromTensor = tensorToImage(imageToTensor(shrekImage))
    imwrite("src/test/test_images/shrek_from_tensor.jpg", shrekFromTensor, Array(CV_8UC3))
  }

  "byteArrayToImage(imageToByteArray(image))" should "presrve image" in {
    // The same situation here
    val shrekImage = imread("src/test/test_images/shrek.jpg")
    val shrekFromBytes = byteArrayToImage(imageToByteArray(shrekImage))
    imwrite("src/test/test_images/shrek_from_bytes.jpg", shrekFromBytes, Array(CV_8UC3))
  }
}