import com.example.app.db.Tables
import com.example.app.servlets._
import com.mchange.v2.c3p0.ComboPooledDataSource
import org.scalatra._
import javax.servlet.ServletContext
import org.slf4j.{Logger, LoggerFactory}
//import slick.jdbc.PostgresProfile.api._
//import slick.jdbc.MySQLProfile.api._
//import slick.jdbc.SQLServerProfile.api._
import slick.jdbc.H2Profile.api._

class ScalatraBootstrap extends LifeCycle {
  val logger: Logger = LoggerFactory.getLogger(getClass)

  val cpds = new ComboPooledDataSource
  logger.info("Created c3p0 connection pool")

  override def init(context: ServletContext) {
    val db = Database.forDataSource(cpds, None)
    db.run(Tables.setup)

    context.mount(new UserServlet(db), "/users/*")
    context.mount(new LoginServlet(db), "/login/*")
    context.mount(new LogoutServlet(db), "/logout/*")
    context.mount(new RegisterServlet(db), "/register/*")
    context.mount(new RoleServlet(db), "/roles/*")
    context.mount(new SpecialtyServlet(db), "/specialties/*")
    context.mount(new AppointmentServlet(db), "/events/*")

    context.initParameters("org.scalatra.cors.allowedOrigins") = "http://localhost:4200"
    context.initParameters("org.scalatra.cors.allowedCredentials") =  "true"

  }
  private def closeDbConnection() {
    logger.info("Closing c3po connection pool")
    cpds.close()
  }

  override def destroy(context: ServletContext) {
    super.destroy(context)
    closeDbConnection()
  }
}
