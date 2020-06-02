import akka.actor.ActorSystem
import com.outworkers.phantom.ResultSet
import datadog.trace.api.Trace

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}

class TestOps(booksOps: BooksOps) {

  def insertBookAndWait(book: Book, ecc: ExecutionContextExecutor): Unit = {
    Await.result(booksOps.insertBook(book, ecc), 5.seconds)
  }

  def multiOperationExpression(book: Book, generalExecutionContext: ExecutionContext, phantomExecutor: ExecutionContextExecutor): Future[Unit] = {
    implicit val ec = generalExecutionContext
    val ops = for {
      rs1 <- booksOps.insertBook(book, phantomExecutor)
      rs2 <- booksOps.setBookStatus(book.id, "Done", phantomExecutor)
      rs3 <- booksOps.setInventory(book.id, 100, phantomExecutor)
    } yield {}
    ops
  }

}
