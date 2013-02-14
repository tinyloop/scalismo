package smptk.registration

import scala.collection.immutable.{ Vector => SVector }
import TransformationSpace.ParameterVector
import Registration.RegistrationResult
import breeze.plot._
import breeze.linalg._
import smptk.image.ContinuousScalarImage1D
import smptk.image.ContinuousDomain
import smptk.image.Geometry._
import smptk.image.ContinuousImageDomain1D
import smptk.image.ContinuousScalarImage
import smptk.image.DiscreteScalarImage1D
import smptk.image.DiscreteImageDomain1D
import smptk.image.Image._
import smptk.image.Interpolation._
import smptk.image.ContinuousImageDomain
import smptk.numerics.Integration
import smptk.image.ContinuousScalarImage1D
import smptk.io.ImageIO
import smptk.image.Interpolation
import smptk.image.Resample
import java.io.File

object Test { 
// def main(args : Array[String])  { 
//        val discreteImage = ImageIO.read2DScalarImage[Short](new File("/tmp/test.h5")).get
//      println("image read")
//        val continuousImage = Interpolation.interpolate2D(0)(discreteImage)
//        println("interpolated")
//      val tSpace = TranslationSpace2D()
//      val translation = tSpace(DenseVector[Float](10,0))
//
//        val translatedImg = continuousImage.warp(translation)
//     println("warped")
//      val resampledImage = Resample.sample2D[Short](translatedImg, discreteImage.domain, 0)      
//      println("resampled")
//      ImageIO.writeImage(resampledImage, new File("/tmp/resampled.h5"))
//      println("written")
//  }
//}

  def doRegistration() = {
//      val oneDTransformSpace = new TransformationSpace[CoordVector1D] {
//      def apply(p: ParameterVector) = {
//        new Transformation[CoordVector1D] { def apply(pt: Point1D) = p(0) * pt(0) }
//      }
//      def parametersDimensionality: Int = 1
//      def takeDerivativeWRTParameters(p: ParameterVector) = { x: Point1D =>
//        val M = DenseMatrix.zeros[Float](1, 1)
//        M(0, 0) = x(0)
//        M
//      }
//    }

    

    val domain = DiscreteImageDomain1D(0f, 0.01f, 3000)
    val domainX = DiscreteImageDomain1D(0f, 0.01f, 3000)
    val domainZ = DiscreteImageDomain1D(0f, 0.01f, 4000)
    val discreteFixedImage = DiscreteScalarImage1D(domain, domain.points.map(x => math.sin(1 * x(0))))
    val fixedImage = interpolate(1)(discreteFixedImage)
//    def isDefinedAt(pt : Point1D) = pt(0) >= 0 && pt(0) <= 30 
//    val fixedImage = ContinuousScalarImage1D(isDefinedAt, 
//        (x : Point1D) => math.sin(x(0).toDouble).toFloat, 
//            (x : Point1D) => DenseVector[Float](math.cos(x(0).toDouble).toFloat))
    val discreteMovingImage = DiscreteScalarImage1D(domainX, domainX.points.map(x => math.sin((x(0) - 1))))
    val movingImage = interpolate(1)(discreteMovingImage)

//    val movingImage = ContinuousScalarImage1D(isDefinedAt, 
//        (x : Point1D) => math.sin(x(0).toDouble - 1).toFloat, 
//            (x : Point1D) => DenseVector[Float](math.cos(x(0).toDouble - 1).toFloat))

    
    val numParameters = 1
    val initialParameters = DenseVector.zeros[Float](numParameters)
    val regularizer = new Regularizer {
      def apply(p: ParameterVector) = 0f
    }
    val transform = TranslationSpace1D()
    //val transform = KernelTransformationSpace1D(domain, numParameters, GaussianKernel1D(1f))
    val regResult = Registration.registration1D(fixedImage, movingImage,  transform, MeanSquaresMetric1D, regularizer, initialParameters)(discreteFixedImage.domain)

    val f = Figure()
    val p = f.subplot(0)

    val warpedImage = movingImage.warp(regResult.transform, fixedImage.isDefinedAt)


    val x = domainZ.points.map(_.toFloat)
   p += plot(x, x.map(f => fixedImage.liftPixelValue(f).getOrElse(0f)))
   p += plot(x, x.map(f => movingImage.liftPixelValue(f).getOrElse(0f)), '.')

    p += plot(x, x.map(f => warpedImage.liftPixelValue(f).getOrElse(0f)),'+')
    // 
    //    p += plot(x, x.map(f => fixedImage(f)))
    //    val warpedimg = movingImage compose regResult.transform
    //    p += plot(x, x.map(f => warpedimg(f)), '+')

    //    val alpha : DenseVector[Float]= linspace(-1, 1).map(_.toFloat)
    //    def warpedImage(alpha : DenseVector[Float]) =[Space] { 
    //      def apply(x : Point1D) = movingImage(oneDTransformSpace(alpha)(x))
    //      def takeDerivative(x : Space#Point) = DenseVector(0)
    //      def domain = fixedImage.domain
    //    }
    //    def metricValue(alpha : Float) = metric(fixedImage, warpedImage(DenseVector(alpha))) 
    //    p += plot(alpha,  alpha.map(metricValue))
    //    
    //    val img = new ContinuousVectorImage[Space] {
    //      val pixelDimensionality = 2
    //      val domain = Simple1DDomain(-1, DenseVector(2f))
    //      def apply(x: Space#Point) = DenseVector(x * x, x* x)
    //      def takeDerivative(x: Space#Point) = DenseVector(0)
    //    }

    regResult
  }

  def main(args: Array[String]) {
    val regResult = doRegistration()
    println("optimal parameters: " + regResult.parameters.toString)

  }

}