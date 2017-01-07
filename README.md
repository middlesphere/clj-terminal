# clj-terminal

## Why we need a terminal? (IoT era is coming)

The GUI interfaces are not always available or suitable for interacting human <-> machine.  
Terminal is fundamentally simple interface available in any environment and suitable for various cases: 
system control, system configuration, backup/restore procedures or other manipulations with systems, 
databases, IoT devices, embeddable systems etc., where GUI interface is not available or broken.
If you build an information security system then terminal interface should be implemented before any GUI interfaces 
and should be always available, cause under some circumstances the terminal may be the only interface available for access to 
security system.
Internet of Things bring us tons of devices where terminal may be the most suitable way for 
interacting human <-> machine.

## About Lanterna

clj-terminal is Clojure wrapper of Lanterna Java library for creating text-based GUIs (https://github.com/mabe02/lanterna)

Lanterna is a Java library allowing you to write easy semi-graphical user interfaces in a text-only environment, 
very similar to the C library curses but with more functionality. Lanterna is supporting xterm compatible terminals 
and terminal emulators such as konsole, gnome-terminal, putty, xterm and many more. One of the main benefits of lanterna 
is that it's not dependent on any native library but runs 100% in pure Java.

Also, when running Lanterna on computers with a graphical environment (such as Windows or Xorg), a bundled terminal 
emulator written in Swing will be used rather than standard output. This way, you can develop as usual from your 
IDE (most of them doesn't support ANSI control characters in their output window) and then deploy to your headless 
server without changing any code.

## Usage

clj-terminal is structured into three layers each built on top of the other and you can easily choose which one 
fits your needs best:
 1. terminal (ns clj-terminal.terminal)
 2. screen (ns clj-terminal.screen)
 3. text gui (ns clj-terminal.gui)

### Terminal layer

Terminal layer is most basic and low-level interface.

In order to get terminal interface just run clj-terminal.terminal/unix-terminal function

```
    (require '[clj-terminal.terminal :as t])
    
    (let [tm (t/unix-terminal)]
        (t/set-fg-color tm :yellow)
        (t/put-string tm "this is basic terminal example\n")
        (t/set-fg-color tm :default))
```
![Image of basic-terminal](https://github.com/middlesphere/clj-terminal/blob/master/examples/resources/basic-example.png)

See examples. (cd examples; lein run basic-terminal)


## License

Copyright © 2016-2017 by Mikhail Ananyev

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
