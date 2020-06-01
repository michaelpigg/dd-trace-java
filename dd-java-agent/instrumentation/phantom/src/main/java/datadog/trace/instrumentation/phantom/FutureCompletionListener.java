package datadog.trace.instrumentation.phantom;

import com.outworkers.phantom.ResultSet;
import datadog.trace.bootstrap.instrumentation.api.AgentScope;
import datadog.trace.bootstrap.instrumentation.api.AgentSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.runtime.AbstractFunction1;
import scala.util.Try;

import static datadog.trace.bootstrap.instrumentation.api.AgentTracer.activateSpan;
import static datadog.trace.bootstrap.instrumentation.api.AgentTracer.activeScope;
import static datadog.trace.instrumentation.phantom.PhantomDecorator.DECORATE;

public class FutureCompletionListener extends AbstractFunction1<Try<ResultSet>, Object> {
  private static final Logger log = LoggerFactory.getLogger(FutureCompletionListener.class);

  private final AgentSpan agentSpan;
  public FutureCompletionListener(AgentSpan agentSpan) {
    this.agentSpan = agentSpan;
  }

  @Override
  public Object apply(final Try<ResultSet> resultSetTry) {
    try {
      final AgentScope scope = activateSpan(agentSpan);
      final ResultSet resultSet = resultSetTry.get();   // TODO: Optimize the potential throw
      System.out.println("Call completed successfully");
      if (resultSet != null) {
        String keyspace = resultSet.getExecutionInfo().getStatement().getKeyspace();
        if (keyspace != null) {
          agentSpan.setTag("keyspace", keyspace);
        }
      }
      DECORATE.beforeFinish(agentSpan);
    } catch (final Throwable t) {
      System.out.println("Call completed with error");
      DECORATE.onError(agentSpan, t);
    } finally {
      System.out.println("");
      agentSpan.finish();
      activeScope().close();
    }
    return null;
  }
}