import akka.actor.{ActorRef, ActorSystem, Props}
import com.example.app.actors.{LoginActor, SSEActor}
import com.example.app.db.Tables
import com.example.app.servlets._
import com.mchange.v2.c3p0.ComboPooledDataSource
import org.scalatra._
import javax.servlet.ServletContext
import org.slf4j.{Logger, LoggerFactory}
import slick.jdbc.H2Profile.api._

class ScalatraBootstrap extends LifeCycle {
  val logger: Logger = LoggerFactory.getLogger(getClass)

  val cpds = new ComboPooledDataSource
  logger.info("Created c3p0 connection pool")

  val db = Database.forDataSource(cpds, None)

  val system = ActorSystem()
  val sseActor: ActorRef = system.actorOf(Props[SSEActor])
  val loginActor: ActorRef = system.actorOf(Props(new LoginActor(db, sseActor)))

  override def init(context: ServletContext) {
    db.run(Tables.setup)

    context.mount(new LoginServlet(db, loginActor), "/login/*")
    context.mount(new LogoutServlet(db), "/logout/*")
    context.mount(new RegisterServlet(db), "/register/*")
    context.mount(new RoleServlet(db), "/roles/*")
    context.mount(new SpecialtyServlet(db), "/specialties/*")
    context.mount(new AppointmentServlet(db), "/events/*")
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
