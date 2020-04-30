package mark.zakharov

import org.bytedeco.javacpp.opencv_imgcodecs.imread
import org.platanios.tensorflow.api.tensors.Tensor
import org.scalatest.{FlatSpec, Matchers, PrivateMethodTester}

class StyleTransferSpec extends FlatSpec with PrivateMethodTester with Matchers {
  "Applying style transfer for the second time" should "not change the image tensor by much" in {
    val shrekImage = imread("src/test/test_images/shrek.jpg")
    val runStyleTransfer = PrivateMethod[Tensor]('runStyleTransfer)
    val shrekImageTensor = CVUtils.imageToTensor(shrekImage)
    val shrekTransformed = StyleTransfer invokePrivate runStyleTransfer(shrekImageTensor)
    val shrekTransformedTransformed = StyleTransfer invokePrivate runStyleTransfer(shrekTransformed)
    val transformedElementsIterator = shrekTransformed.entriesIterator
    val transformedTransformedElementsIterator = shrekTransformedTransformed.entriesIterator
    val differences: Array[Double] = Array.ofDim(300 * 300 * 3)
    for (i <- 0 until 300 * 300 * 3) {
      // I did not find another way of converting DataType to a Scala numeric type
      val t = transformedElementsIterator.next().toString.toDouble
      val tt = transformedTransformedElementsIterator.next().toString.toDouble
      differences.update(i, t - tt)
    }
    (differences.sum / differences.size < 5) shouldBe true
  }
}
