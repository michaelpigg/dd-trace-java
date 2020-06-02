package datadog.trace.instrumentation.phantom;

import com.datastax.driver.core.Session;
import com.outworkers.phantom.ResultSet;
import com.outworkers.phantom.ops.QueryContext;
import datadog.trace.bootstrap.instrumentation.api.AgentScope;
import datadog.trace.bootstrap.instrumentation.api.AgentSpan;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.Future;

import static datadog.trace.bootstrap.instrumentation.api.AgentTracer.activateSpan;
import static datadog.trace.bootstrap.instrumentation.api.AgentTracer.startSpan;
import static datadog.trace.instrumentation.phantom.PhantomDecorator.DECORATE;

public class PhantomAdvice {
  @Advice.OnMethodEnter(suppress = Throwable.class)
  public static AgentScope enter(@Advice.This final QueryContext.RootQueryOps rootQueryOps,
                                 @Advice.Argument(value = 0) final Session session,
                                 @Advice.Argument(value = 1) final ExecutionContextExecutor ctx ) {
    System.out.println("Calling with context " + ctx.toString());
    final AgentScope scope = startSpanWithScope(rootQueryOps);
    scope.setAsyncPropagation(true);
    return scope;
  }

  public static AgentScope startSpanWithScope(final QueryContext.RootQueryOps queryOps) {
    final AgentSpan span = startSpan("phantom.future");
    DECORATE.afterStart(span);
    DECORATE.onStatement(span, queryOps.query().queryString());
    //log.debug("activating span " + span);
    return activateSpan(span);
  }

  @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
  public static void exit(
    @Advice.Argument(value = 1) final ExecutionContextExecutor ctx,
    @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Future<ResultSet> resultSetFuture,
    @Advice.Enter final AgentScope agentScope) {
    //log.debug("onMethodExit " + agentScope.toString());
    if (agentScope == null || resultSetFuture == null) {
      return;
    }

    resultSetFuture.onComplete(new FutureCompletionListener(agentScope.span()), ctx);
  }
}
