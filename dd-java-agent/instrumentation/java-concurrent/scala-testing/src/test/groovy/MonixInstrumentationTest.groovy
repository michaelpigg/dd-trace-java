import datadog.trace.agent.test.AgentTestRunner
import datadog.trace.core.DDSpan

class MonixInstrumentationTest extends AgentTestRunner {

  def "monix tasks"() {
    setup:
    ScalaMonixTests monixTest = new ScalaMonixTests()
    int expectedNumberOfSpans = monixTest.traceTask()
    TEST_WRITER.waitForTraces(1)
    List<DDSpan> trace = TEST_WRITER.get(0)

    expect:
    trace.size() == expectedNumberOfSpans
    trace[0].resourceName == "ScalaMonixTests.traceTask"
    //trace[0].operationName == "simple-task"
    findSpan(trace, "simple-task")

  }

  def "monix tasks for expression"() {
    setup:
    ScalaMonixTests monixTest = new ScalaMonixTests()
    int expectedNumberOfSpans = monixTest.traceTaskFor()
    TEST_WRITER.waitForTraces(1)
    List<DDSpan> trace = TEST_WRITER.get(0)

    expect:
    trace.size() == expectedNumberOfSpans
    trace[0].resourceName == "ScalaMonixTests.traceTaskFor"
    findSpan(trace, "for-task-1")
    findSpan(trace, "for-task-2")

  }

  def "monix runAsync"() {
    setup:
    ScalaMonixTests monixTest = new ScalaMonixTests()
    int expectedNumberOfSpans = monixTest.traceAsyncTask()
    TEST_WRITER.waitForTraces(1)
    List<DDSpan> trace = TEST_WRITER.get(0)

    expect:
    trace.size() == expectedNumberOfSpans
    trace[0].resourceName == "ScalaMonixTests.traceAsyncTask"
    findSpan(trace, "trace-async-task")
  }

  private DDSpan findSpan(List<DDSpan> trace, String opName) {
    for (DDSpan span : trace) {
      if (span.getOperationName() == opName) {
        return span
      }
    }
    return null
  }
}
