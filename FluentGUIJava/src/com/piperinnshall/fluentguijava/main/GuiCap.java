package com.piperinnshall.fluentguijava.main;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

class GuiCap {
  void runGui(String title, GuiScope gs) {
    var done = new CompletableFuture<RuntimeException>();
    var builder = new CGuiBuilder();
    gs.run(builder);
    SwingUtilities.invokeLater(() -> builder.start(title, done));
    var tr = done.join(); // TODO: check if the cf can 'correctly' return null
    if (tr != null) {
      throw tr;
    }
  }
}

interface GuiScope {
  void run(GuiBuilder b);
}

class Slot<T> {
  private Optional<T> inner = Optional.empty();

  static <T> Slot<T> of() {
    return new Slot<>();
  }

  void fill(T t) {
    inner = Optional.of(t);
  }

  T get() {
    return inner.get();
  }
}

interface GuiBuilder {
  GuiBuilder button(String text);

  GuiBuilder button(String text, Runnable r);

  GuiBuilder button(String text, Runnable r, Slot<Button> s);
}

interface Button {
  String text(String text);

  String text();
}

record CButton(JButton b) implements Button {
  @Override
  public String text(String text) {
    b.setText(text);
    return text;
  }

  @Override
  public String text() {
    return b.getText();
  }
}

class CGuiBuilder implements GuiBuilder {
  List<Consumer<JComponent>> rs = new ArrayList<>();
  Consumer<Runnable> submit;

  public void start(String title, CompletableFuture<RuntimeException> done) {
    var o = new Object() {
      Consumer<RuntimeException> c = t -> {
        this.f.dispatchEvent(new WindowEvent(this.f, WindowEvent.WINDOW_CLOSING));
        done.complete(t);
      };
      volatile SerialQueue exe = new SerialQueue(c);
      FearlessGui f = new FearlessGui(title, done, exe);
    };
    var myRoot = new JPanel();
    rs.forEach(cjc -> cjc.accept(myRoot));
    o.f.add(myRoot);
    o.f.setPreferredSize(new Dimension(300, 300));
    o.f.pack();
    o.f.setVisible(true);
    submit = o.exe::submit;
  }

  @Override
  public GuiBuilder button(String text) {
    rs.add(parent -> parent.add(new JButton(text)));
    return this;
  }

  @Override
  public GuiBuilder button(String text, Runnable r) {
    rs.add(parent -> {
      var b = new JButton(text);
      b.addActionListener(_ -> submit.accept(r));
      parent.add(b);
    });
    return this;
  }

  @Override
  public GuiBuilder button(String text, Runnable r, Slot<Button> s) {
    rs.add(parent -> {
      var b = new JButton(text);
      b.addActionListener(_ -> submit.accept(r));
      parent.add(b);
      s.fill(new CButton(b));
    });
    return this;
  }
}

@SuppressWarnings("serial")
class FearlessGui extends JFrame {
  FearlessGui(String title, CompletableFuture<RuntimeException> done, SerialQueue exe) {
    super(title);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        exe.closeAndWait();
        done.complete(null);
      }
    });
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
  }
}
