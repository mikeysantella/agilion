package net.deelam.spark;
//    import scala.language.implicitConversions

object JavaFunctionUtil{
  
  import java.util.function._
  implicit def toConsumer[A](function: A => Any): Consumer[A] = new Consumer[A]() {
    override def accept(arg: A): Unit = { function.apply(arg); Unit; }
  }
  

}