# Design

No exposing swing types?

3 sibling Windows

```
JFrame
JDialog
JWindow
```

Each are children of `awt.Window`.

In FluentGUI.java:

```
FluentWindow = Frame.of();
FluentWindow = Dialog.of();
FluentWindow = Window.of();
```
