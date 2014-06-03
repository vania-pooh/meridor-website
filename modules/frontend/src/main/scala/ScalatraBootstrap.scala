import ru.meridor.website.db.ConnectionPooler
import ru.meridor.website._
import org.scalatra._
import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context.mount(new PagesServlet, "/*")
    context.initParameters("org.scalatra.environment") = "production" //This disables ugly Scalatra 404 page
  }

  override def destroy(context: ServletContext) {
    super.destroy(context)
    ConnectionPooler.shutdown()
  }
}
