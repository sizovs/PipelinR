package an.awesome.pipelinr;

import static java.lang.Thread.currentThread;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class NotificationsTest {

  private AtomicLong timesHandled = new AtomicLong();
  private Collection<Thread> threads = new HashSet<>();

  @Test
  void executesMiddlewareInOrder() {
    // given
    List<String> invokedMiddlewareIds = new ArrayList<>();

    // and
    class IdentifiableMiddleware implements Notification.Middleware {
      private final String id;

      private IdentifiableMiddleware(String id) {
        this.id = id;
      }

      @Override
      public <N extends Notification> void invoke(N notification, Next next) {
        invokedMiddlewareIds.add(id);
        next.invoke();
      }
    }

    // and
    class Greet implements Notification {}

    class OnGreet implements Notification.Handler<Greet> {

      @Override
      public void handle(Greet notification) {
        System.out.println("OK");
      }
    }

    // and
    Notification.Middleware foo = new IdentifiableMiddleware("foo");

    // and
    Notification.Middleware bar = new IdentifiableMiddleware("bar");

    // and
    Notification.Middleware baz = new IdentifiableMiddleware("baz");

    // and
    Pipelinr pipelinr =
        new Pipelinr().with(() -> Stream.of(new OnGreet())).with(() -> Stream.of(foo, bar, baz));

    // when
    new Greet().send(pipelinr);

    // then
    assertThat(invokedMiddlewareIds).containsExactly("foo", "bar", "baz");
  }

  @Test
  void supportsAsyncStrategy() {
    // given:
    ExecutorService threadPool = Executors.newFixedThreadPool(1);
    Pipeline pipeline =
        pipelineWithHandlers(
                new ThrowingRepublican("Omg"),
                new ThrowingRepublican("Oh!"),
                new Bush(),
                new Trump())
            .with(() -> new Async(threadPool));

    // when:
    RuntimeException e =
        assertThrows(RuntimeException.class, () -> new GreetRepublicans().send(pipeline));

    // then:
    assertThat(e).isExactlyInstanceOf(AggregateException.class);
    assertThat(timesHandled).hasValue(4);
    assertThat(threads).hasSize(1);
    assertThat(threads).doesNotContain(currentThread());
  }

  @Test
  void supportsCustomHandlerMatching() {
    Collection<String> notificationTexts = new CopyOnWriteArrayList<>();

    class TextNotification implements Notification {
      private final String text;

      TextNotification(String text) {
        this.text = text;
      }
    }
    class HandlerForFoo implements Notification.Handler<TextNotification> {
      @Override
      public void handle(TextNotification notification) {
        notificationTexts.add(notification.text);
      }

      @Override
      public boolean matches(TextNotification notification) {
        return notification.text.equals("foo");
      }
    }

    class HandlerForBar implements Notification.Handler<TextNotification> {
      @Override
      public void handle(TextNotification notification) {
        notificationTexts.add(notification.text);
      }

      @Override
      public boolean matches(TextNotification notification) {
        return notification.text.equals("bar");
      }
    }

    class HandlerForAll implements Notification.Handler<TextNotification> {
      @Override
      public void handle(TextNotification notification) {
        notificationTexts.add(notification.text);
      }
    }

    new Pipelinr()
        .with(() -> Stream.of(new HandlerForFoo(), new HandlerForBar(), new HandlerForAll()))
        .send(new TextNotification("foo"));
    assertThat(notificationTexts).containsOnly("foo");
    assertThat(notificationTexts).hasSize(2);

    notificationTexts.clear();

    new Pipelinr()
        .with(() -> Stream.of(new HandlerForFoo(), new HandlerForBar(), new HandlerForAll()))
        .send(new TextNotification("bar"));
    assertThat(notificationTexts).containsOnly("bar");
    assertThat(notificationTexts).hasSize(2);
  }

  @Test
  void supportsParallelNoWait() throws InterruptedException {
    // given:
    ExecutorService threadPool = Executors.newFixedThreadPool(4);
    Pipeline pipeline =
        pipelineWithHandlers(
                new ThrowingRepublican("Omg"),
                new ThrowingRepublican("Oh!"),
                new Bush(),
                new Trump())
            .with(() -> new ParallelNoWait(threadPool));

    // when:
    new GreetRepublicans().send(pipeline);
    threadPool.awaitTermination(3, TimeUnit.SECONDS);

    // then:
    assertThat(timesHandled).hasValue(4);
    assertThat(threads).hasSize(4);
    assertThat(threads).doesNotContain(currentThread());
  }

  @Test
  void supportsParallelWhenAllStrategy() {
    // given:
    ExecutorService threadPool = Executors.newFixedThreadPool(4);
    Pipeline pipeline =
        pipelineWithHandlers(
                new ThrowingRepublican("Omg"),
                new ThrowingRepublican("Oh!"),
                new Bush(),
                new Trump())
            .with(() -> new ParallelWhenAll(threadPool));

    // when:
    RuntimeException e =
        assertThrows(RuntimeException.class, () -> new GreetRepublicans().send(pipeline));

    // then:
    assertThat(e).isExactlyInstanceOf(AggregateException.class);
    assertThat(e).hasMessageContaining("2 exception(s)");
    assertThat(timesHandled).hasValue(4);
    assertThat(threads).hasSize(4);
    assertThat(threads).doesNotContain(currentThread());
  }

  @Test
  void supportsParallelWhenAnyStrategy() throws InterruptedException {
    // given:
    ExecutorService threadPool = Executors.newFixedThreadPool(4);
    Pipeline pipeline =
        pipelineWithHandlers(
                new ThrowingRepublican("Omg", 2000),
                new ThrowingRepublican("Oh!", 2000),
                new ThrowingRepublican("Nah"),
                new ThrowingRepublican("Boo", 2000))
            .with(() -> new ParallelWhenAny(threadPool));

    // when:
    RuntimeException e =
        assertThrows(RuntimeException.class, () -> new GreetRepublicans().send(pipeline));

    // then:
    assertThat(e).isExactlyInstanceOf(AggregateException.class);
    assertThat(e).hasMessageContaining("1 exception(s)");

    threadPool.awaitTermination(5, TimeUnit.SECONDS);
    assertThat(timesHandled).hasValue(4);
    assertThat(threads).hasSize(4);
    assertThat(threads).doesNotContain(currentThread());
  }

  @Test
  void supportsContinueOnExceptionStrategyThrowingAggregateException() {
    // when:
    RuntimeException e =
        assertThrows(
            RuntimeException.class,
            () ->
                new GreetRepublicans()
                    .send(
                        pipelineWithHandlers(
                                new ThrowingRepublican("Omg"),
                                new ThrowingRepublican("Oh!"),
                                new Bush(),
                                new Trump())
                            .with(ContinueOnException::new)));

    // then:
    assertThat(e).isExactlyInstanceOf(AggregateException.class);
    assertThat(e).hasMessageContaining("2");
    assertThat(timesHandled).hasValue(4);
    assertThat(threads).containsExactly(currentThread());
  }

  @Test
  void supportsContinueOnExceptionSuccessfulHandling() {
    assertDoesNotThrow(
        () ->
            new GreetRepublicans()
                .send(
                    pipelineWithHandlers(new Bush(), new Trump()).with(ContinueOnException::new)));

    assertThat(timesHandled).hasValue(2);
    assertThat(threads).containsExactly(currentThread());
  }

  @Test
  void stopOnExceptionIsDefaultStrategy() {
    // given:
    Pipeline pipeline =
        pipelineWithHandlers(
            new ThrowingRepublican("Omg"), new ThrowingRepublican("Oh!"), new Bush(), new Trump());

    // when:
    RuntimeException e =
        assertThrows(RuntimeException.class, () -> new GreetRepublicans().send(pipeline));

    // then:
    assertThat(timesHandled).hasValue(1);
    assertThat(e).hasMessage("Omg");
    assertThat(threads).containsExactly(currentThread());
  }

  @Test
  void supportsNotificationsWithGenerics() {
    class GenericNotification<X> implements Notification {}

    class GenericNotificationHandler extends BaseNotificationHandler<GenericNotification<String>> {}

    Pipeline pipeline = new Pipelinr().with(() -> Stream.of(new GenericNotificationHandler()));

    new GenericNotification<>().send(pipeline);
    assertThat(timesHandled).hasValue(1);
  }

  @Test
  void sendsNotificationToAllHandlers() {
    // given:
    Pipeline pipeline = pipelineWithHandlers(new Bush(), new Trump(), new Hilary());

    // when:
    new GreetRepublicans().send(pipeline);

    // then:
    assertThat(timesHandled).hasValue(2);
  }

  private Pipelinr pipelineWithHandlers(Notification.Handler... handlers) {
    return new Pipelinr().with(() -> Stream.of(handlers));
  }

  static class GreetDemocrats implements Notification {}

  static class GreetRepublicans implements Notification {}

  class Hilary extends BaseNotificationHandler<GreetDemocrats> {}

  class Bush extends BaseNotificationHandler<GreetRepublicans> {}

  class Trump extends BaseNotificationHandler<GreetRepublicans> {}

  class ThrowingRepublican extends BaseNotificationHandler<GreetRepublicans> {
    private final String message;
    private final long sleepMillis;

    ThrowingRepublican(String message) {
      this.message = message;
      this.sleepMillis = 0;
    }

    public ThrowingRepublican(String message, long sleepMillis) {
      this.message = message;
      this.sleepMillis = sleepMillis;
    }

    @Override
    public void handleNow(GreetRepublicans notification) {
      try {
        Thread.sleep(sleepMillis);
      } finally {
        throw new RuntimeException(message);
      }
    }
  }

  abstract class BaseNotificationHandler<N extends Notification>
      implements Notification.Handler<N> {

    @Override
    public final void handle(N notification) {
      threads.add(currentThread());
      timesHandled.incrementAndGet();
      handleNow(notification);
    }

    protected void handleNow(N notification) {}
  }
}
