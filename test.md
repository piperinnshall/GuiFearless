## chore: Add `width'` and `height'` to gui

The existing `.width` and `.height` methods on `Widget[S]` took `WidthNat` and
`HeightNat` respectively. This was inconsistent with other methods like
`.radius(Nat)` that take plain `Nat.` This commit renames the typed overloads
to `.width'` / `.height'` and introduces `.width(Nat)` / `.height(Nat)` that
wrap internally.

#### `types.fear`:

`.width(Nat)` / `.height(Nat)` is new and wraps to `WidthNat` / `HeightNat`
internally `.width'(WidthNat)` / `.height'(HeightNat)`. It's renamed from the
old `.width` / `.height`.

#### `_Widgets.java`:

`mut$width$1` / `mut$height$1` wrap the incoming `Nat$0` via
`WidthNat$0.instance.read$$hash$1` before storing `mut$width$p1$1` /
`mut$height$p1$1` (the ' variants) unchanged

## feat: Add button-like `modelFps` that triggers an action x times per second 

Adds `Frame.modelFps(Nat, Scope[ModelFps])`. It's a headless timer that fires
registered actions at a fixed rate, decoupled from the render FPS. It's
similar to an invisible button that clicks itself on a schedule.

It's used in the form:

```
.modelFps (1, {::
  .action{Debug#"tick done"}
  .action{this.model.tick}
  })
```

#### `types.fear`:

```
ModelFps: {
  mut .action(mut MF[Void]): mut ModelFps;
}
// on Frame:
mut .modelFps(Nat, mut Scope[ModelFps]): mut Frame;
```

#### FearlessFrame.java:

Adds Timer `modelTimer` alongside the existing render timer. `startModelTimer`
starts it; `stopTimer` now stops both.

#### `_Widgets.java`:

`_Frame` collects actions during `mut$modelFps$2,` then starts the model timer
at `start()` time with an immutable snapshot of the action list. Each tick
submits all actions to the serial queue. This is the same queue as button clicks
and key events.

