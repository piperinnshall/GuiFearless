package com.piperinnshall.fluentguijava.main;

import java.util.ArrayList;
import java.util.List;

record Model(List<String> log) {
  void add(String s) {
    log.add(s);
  }
}

record View(Model m, Slot<Button> btn) {
  void onAdd() {
    m.add("added");
    btn.get().text("" + m.log().size());
  }

  void build() {
    new GuiCap().runGui("Counter", b -> b
        .button("Add", this::onAdd)
        .button("0", this::onAdd, btn));
  }
}

public class Main {
  public static void main(String[] a) {
    var m = new Model(new ArrayList<>());
    new View(m, Slot.of()).build();
    System.out.print(m);
  }
}

/*
record View(Model m, Slot<Button> s, Slot<Tween> x) {
  void onAdd() {
    m.add("added");
    s.get().text("" + m.log().size());
  }
  void build() {
    new GuiCap().runGui("Counter", b -> b
        .button("0", this::onAdd, s)
        .anim(a -> a.tween(0.0, 200.0, 2.0, Easing.EASE_IN_OUT, x))
        .canvas(60, d -> d.circle(x, 100, 20)));
  }
}

public class Main {
  public static void main(String[] a) {
    var m = new Model(new ArrayList<>());
    new View(m, Slot.of(), Slot.of()).build();
  }
}
*/
