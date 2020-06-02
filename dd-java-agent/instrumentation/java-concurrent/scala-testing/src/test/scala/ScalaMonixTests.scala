import datadog.trace.api.Trace
import datadog.trace.bootstrap.instrumentation.api.AgentTracer.activeSpan
import monix.eval.Task
import monix.execution.Callback
import monix.execution.Scheduler
import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}


class ScalaMonixTests {

  val globalScheduler = Scheduler.global
  val ioScheduler = Scheduler.io("scala-monix-tests")

  def shutdown(): Unit = {
    ioScheduler.shutdown()
  }

  @Trace
  def tracedChild(opName: String): Unit = {
    activeSpan().setSpanName(opName)
  }

  @Trace
  def traceTask: Int = {
    val f = Task.eval(tracedChild("simple-task"))
    Await.result(f.runToFuture(Scheduler.global), 5.seconds)
    2
  }

  @Trace
  def traceTaskFor: Int = {
    val f = for {
      t <- Task.eval(tracedChild("for-task-1"))
      u <- Task.eval(tracedChild("for-task-2"))
    } yield 3
    Await.result(f.runToFuture(Scheduler.global), 5.seconds)
  }

  @Trace
  def traceAsyncTask: Int = {
    val p = Promise[Int]()
    val f = for {
      t <- Task.eval(tracedChild("trace-async-task"))
    } yield 2
    f.runAsync(Callback.fromPromise(p))(Scheduler.global)
    Await.result(p.future, 5.seconds)
  }

  @Trace
  def traceAcrossExecutors: Int = {
    implicit val sc = globalScheduler
    val f = for {
      t <- Task.eval(tracedChild("global-1"))
      u <- Task.eval(tracedChild("io-1")).executeOn(ioScheduler)
      v <- Task.eval(tracedChild("global-2"))
    } yield 4
    Await.result(f.runToFuture, 5.seconds)
  }

  @Trace
  def traceNestedSpans: Int = {
    implicit val sc = globalScheduler
    val t = Task.eval(tracedChild("nested-span", 2))
    Await.result(t.runToFuture, 5.seconds)
    4
  }

  @Trace
  def tracedChild(opName: String, level: Int): Unit = {
    activeSpan().setSpanName(s"$opName-$level")
    if (level > 0) {
      tracedChild(opName, level - 1)
    }
  }
}
