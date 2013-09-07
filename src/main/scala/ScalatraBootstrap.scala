import ru.meridor.diana.db.ConnectionPooler
import ru.meridor.website._
import org.scalatra._
import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context.mount(new PagesServlet, "/*")
    context.mount(new ApiServlet, "/api/*")
  }

  override def destroy(context: ServletContext) {
    super.destroy(context)
    ConnectionPooler.shutdown()
  }
}
