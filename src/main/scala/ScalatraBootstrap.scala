import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.stream.{DelayOverflowStrategy, OverflowStrategy}
import akka.stream.scaladsl.{BroadcastHub, Keep, Source}
import com.example.app.actors.{LoginActor, SSEActor}
import com.example.app.db.Tables
import com.example.app.servlets._
import com.mchange.v2.c3p0.ComboPooledDataSource
import org.scalatra._
import javax.servlet.ServletContext
import org.slf4j.{Logger, LoggerFactory}
import slick.jdbc.H2Profile.api._
import scala.concurrent.duration._

class ScalatraBootstrap extends LifeCycle {
  val logger: Logger = LoggerFactory.getLogger(getClass)

  val cpds = new ComboPooledDataSource
  logger.info("Created c3p0 connection pool")

  val db = Database.forDataSource(cpds, None)

  val (sourceQueue, eventsSource) = Source.queue[String](Int.MaxValue, OverflowStrategy.backpressure)
    .delay(1.seconds, DelayOverflowStrategy.backpressure)
    .map(message => ServerSentEvent(message))
    .keepAlive(1.second, () => ServerSentEvent.heartbeat)
    .toMat(BroadcastHub.sink[ServerSentEvent])(Keep.both)
    .run()

  val system = ActorSystem()
  val sseActor: ActorRef = system.actorOf(Props(new SSEActor(sourceQueue)))
  val loginActor: ActorRef = system.actorOf(Props(new LoginActor(db, sseActor)))

  override def init(context: ServletContext) {
    db.run(Tables.setup)

    context.mount(new LoginServlet(db, loginActor), "/login/*")
    context.mount(new LogoutServlet(db), "/logout/*")
    context.mount(new RegisterServlet(db), "/register/*")
    context.mount(new RoleServlet(db), "/roles/*")
    context.mount(new SpecialtyServlet(db), "/specialties/*")
    context.mount(new AppointmentServlet(db), "/events/*")
    context.mount(new SSEServlet(db, eventsSource), "/sse/*")
  }

  private def closeDbConnection() {
    logger.info("Closing c3po connection pool")
    cpds.close()
  }

  override def destroy(context: ServletContext) {
    super.destroy(context)
    closeDbConnection()
    system.terminate()
  }
}
