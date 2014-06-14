object App {
  import unfiltered.netty.websockets._
  import unfiltered.util._
  import scala.collection.mutable.ConcurrentMap
  import unfiltered.response.ResponseString

  def main(args: Array[String]) {
    import scala.collection.JavaConversions._
    val sockets: ConcurrentMap[Int, WebSocket] =
      new java.util.concurrent.ConcurrentHashMap[Int, WebSocket]

    unfiltered.netty.Http(5679).handler(unfiltered.netty.websockets.Planify({
      case _ => {
        case Message(s, Text(msg)) =>

        case Open(s) => sockets += (s.channel.getId.intValue -> s)
        case Close(s) => sockets -= s.channel.getId.intValue
        case Error(s, e) => e.printStackTrace
      }
    })
  }
}