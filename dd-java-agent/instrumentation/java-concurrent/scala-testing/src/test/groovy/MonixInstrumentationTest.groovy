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

  def "test across schedulers"() {
    setup:
    ScalaMonixTests monixTest = new ScalaMonixTests()
    int expectedNumberOfSpans = monixTest.traceAcrossExecutors()
    TEST_WRITER.waitForTraces(1)
    List<DDSpan> trace = TEST_WRITER.get(0)

    expect:
    trace.size() == expectedNumberOfSpans
    trace[0].resourceName == "ScalaMonixTests.traceAcrossExecutors"
    findSpan(trace, "global-1")
    findSpan(trace, "global-2")
    findSpan(trace, "io-1")

  }

  def "test nested spans"() {
    setup:
    ScalaMonixTests monixTest = new ScalaMonixTests()
    int expectedNumberOfSpans = monixTest.traceNestedSpans()
    TEST_WRITER.waitForTraces(1)
    List<DDSpan> trace = TEST_WRITER.get(0)

    expect:
    trace.size() == expectedNumberOfSpans
    trace[0].resourceName == "ScalaMonixTests.traceNestedSpans"
    DDSpan span2 = findSpan(trace, "nested-span-2")
    DDSpan span1 = findSpan(trace, "nested-span-1")
    DDSpan span0 = findSpan(trace, "nested-span-0")
    span0.parentId == span1.spanId
    span1.parentId == span2.spanId
    span2.parentId == trace[0].spanId
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
