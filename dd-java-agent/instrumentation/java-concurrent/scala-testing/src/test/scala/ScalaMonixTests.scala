import datadog.trace.api.Trace
import datadog.trace.bootstrap.instrumentation.api.AgentTracer.activeSpan
import monix.eval.Task
import monix.execution.Callback
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}


class ScalaMonixTests {

  @Trace
  def tracedChild(opName: String): Unit = {
    activeSpan().setSpanName(opName)
  }

  @Trace
  def traceTask: Int = {
    val f = for {
      t <- Task.eval(tracedChild("simple-task"))
    } yield 2
    Await.result(f.runToFuture, 5.seconds)
  }

  @Trace
  def traceTaskFor: Int = {
    val f = for {
      t <- Task.eval(tracedChild("for-task-1"))
      u <- Task.eval(tracedChild("for-task-2"))
    } yield 3
    Await.result(f.runToFuture, 5.seconds)
  }

  @Trace
  def traceAsyncTask: Int = {
    val p = Promise[Int]()
    val f = for {
      t <- Task.eval(tracedChild("trace-async-task"))
    } yield  2
    f.runAsync(Callback.fromPromise(p))
    Await.result(p.future, 5.seconds)
  }
}
