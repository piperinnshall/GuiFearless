package com.piperinnshall.fluentguijava.swing;

import javax.swing.SwingUtilities;

record Timer(Runnable doRun, Thread t) {
  Timer(int fps, Runnable doRun) {
    this(doRun, new Thread(() -> {
      long next = System.nanoTime();
      for (;;) {
        next += 1_000_000_000L / fps;
        SwingUtilities.invokeLater(doRun);
        long delay = next - System.nanoTime();
        if (delay > 0)
          try {
            // Nanoseconds to Milliseconds
            Thread.sleep(Math.round(delay / 1_000_000.0));
          } catch (InterruptedException _) {
            break;
          }
      }
    }));
  }

  void start() {
    t.start();
  }

  void stop() {
    t.interrupt();
  }
}
