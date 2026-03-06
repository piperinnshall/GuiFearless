package com.piperinnshall.fluentguijava.main;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
 * Serial queue of tasks, dies as soon as any task throws errors. Interrupts
 * also kills the queue.
 */
public record SerialQueue(BlockingQueue<Runnable> q, Thread t) {
  public SerialQueue(Consumer<RuntimeException> onError) { this(new LinkedBlockingQueue<>(), onError); }
  private SerialQueue(BlockingQueue<Runnable> q, Consumer<RuntimeException> onError) { this(q, makeThread(q, onError)); }
  private static Thread makeThread(BlockingQueue<Runnable> q, Consumer<RuntimeException> onError) {
    var t = new Thread(() -> runLoop(q), "GuiToMain");
    t.setUncaughtExceptionHandler((_, e) -> {
      if (e instanceof Poison) return;
      if (e instanceof RuntimeException re) { onError.accept(re); return;
      }
      onError.accept(new RuntimeException("Unexpected throwable", e));
    });
    t.start();
    return t;
  }
  public void submit(Runnable r) { synchronized (q) { q.add(r); } }
  public void closeAndWait() {
    synchronized (q) { q.clear(); q.add(() -> { throw new Poison(); }); }
    if (Thread.currentThread() == t) return;
    try {
      t.join();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new Error(e);
    }
  }
  private static void runLoop(BlockingQueue<Runnable> q) {
    for (;;) {
      try {
        q.take().run();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new Error(e);
      }
    }
  }
  private static class Poison extends RuntimeException {}
}
