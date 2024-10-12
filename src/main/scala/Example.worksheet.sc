import scala.deriving.Mirror
import scala.collection.mutable.Stack

abstract class Student(age:Int){
    def hello= println(s"Hello people,my age is ${age}")
    hello
}

final case class UnderGraduate(age:Int) extends Student(age)


val student= UnderGraduate(12)

90

val sk=Stack(6)

sk.pop()
sk.push(1)
sk.push(2)
sk.push(3)
sk.push(4)

sk.head

sk

def getKey(key:Int,value:String)=key

getKey.tupled(1->"One")

case class KeyValue(key:Int,value:String)

val tuple=Tuple.fromProductTyped(KeyValue(1,"Two"))

val keyValue=summon[Mirror.Of[KeyValue]].fromProduct(tuple)


Math.pow(2,1)* 0.2




