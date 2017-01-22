# clj-terminal

## Why we need a terminal? (IoT era is coming)

The GUI interfaces are not always available or suitable for interacting human <-> machine.  
Terminal is fundamentally simple interface available in any environment and suitable for various cases: 
system control, system configuration, backup/restore procedures or other manipulations with systems, 
databases, IoT devices, embeddable systems etc., where GUI interface is not available or broken.
If you build an information security system then terminal interface should be implemented before any GUI interfaces 
and should be always available, cause under some circumstances the terminal may be the only interface available for access to 
security system.
Internet of Things bring us a tons of devices where terminal may be the most suitable way for 
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

## Intro

clj-terminal is structured into three layers each built on top of the other and you can easily choose which one 
fits your needs best:
 1. terminal (ns clj-terminal.terminal)
 2. screen (ns clj-terminal.screen)
 3. text gui (ns clj-terminal.gui)

## Terminal layer


Terminal layer is most basic and low-level interface. 
```clojure
    (require '[clj-terminal.terminal :as t])
```

In order to get terminal interface just 
run (clj-terminal.terminal/unix-terminal) function:

```clojure
    (let [tm (t/unix-terminal)]
        (t/set-fg-color tm :yellow)
        (t/put-string tm "this is basic terminal example\n")
        (t/set-fg-color tm :default))
```
See examples. (cd examples; lein run basic-terminal)
![Image of basic-terminal](https://github.com/middlesphere/clj-terminal/blob/master/examples/resources/basic-example.png)

If you need to prevent terminate program by Ctrl-C then to get terminal interface use function:

```clojure
    (t/unix-terminal-without-ctrl-c) 
```

Lanterna library has many types of terminals (swing, cygwin, unix...) but in clj-terminal the Terminal layer has only 
unix terminal which works in Linux/Unix and MacOS. If you need to work with clj-terminal in Windows (cygwin), 
in Swing terminal or you build OS-independent application then you have to use next layer - Screen (ns clj-terminal.screen).  

### Private mode

Private mode allows you:

* to use private area for terminal;
* preserve terminal history and restore it after exit private mode;
* clear screen and put the cursor in the top-left corner when enter to private mode;
* disable the scrolling and establish a fixed screen.
    
To enter to a private mode use t/enter-private-mode function.

Example:
```clojure
(let [tm (t/unix-terminal)]
    (t/enter-private-mode tm)
    (t/put-string tm "this is private mode demo\nPress ENTER to exit.")
    (read-line)
    (t/exit-private-mode tm))
```
![Image of private-mode](https://github.com/middlesphere/clj-terminal/blob/master/examples/resources/private-mode.png)

Notice, when exit from private mode all terminal state is restored.


### Writing text

To print characters on terminal you may use (t/put-string) function to print string, or (t/put-character) function
to print an individual character. These functions prints characters in a current cursor position. Here is an example:
```clojure
(let [tm (t/unix-terminal)]
    (t/put-character tm \H) (t/put-character tm \e) (t/put-character tm \l) (t/put-character tm \l)
    (t/put-character tm \o) (t/put-character tm \!)
    (t/put-string tm "\nPress ENTER to exit.\n")
    (read-line))
```
![image of print-chars](https://github.com/middlesphere/clj-terminal/blob/master/examples/resources/print-chars.png)

But what, if we need to print chars at specific cursor position? So, just add cursor position like this
(t/put-string tm s col row) or (t/put-character tm c col row). Here is an example:

```clojure
(let [tm (t/unix-terminal)]
    (t/enter-private-mode tm)

    (t/put-character tm \H 2 6) (t/put-character tm \e 2 5) (t/put-character tm \l 2 4) (t/put-character tm \l 2 3)
    (t/put-character tm \o 2 2) (t/put-character tm \! 2 1)

    (t/put-string tm "Press ENTER to exit." 4 3)

    (read-line)
    (t/exit-private-mode tm))
```
![image of print-chars](https://github.com/middlesphere/clj-terminal/blob/master/examples/resources/print-chars-pos.png)



### Moving cursor

In order to move cursor on terminal use function (t/set-cursor-position tm col row). To get current cursor position use 
(t/get-cursor-position tm).

```clojure
(let [tm (t/unix-terminal)]
    (t/enter-private-mode tm)
    (t/set-cursor-position tm 5 5 )
    (t/put-string tm (str (t/get-cursor-position tm)))
    (read-line)
    (t/exit-private-mode tm))
```

![image of print-chars](https://github.com/middlesphere/clj-terminal/blob/master/examples/resources/cur-pos.png)


### Colors (background and foreground)

You can change the foreground and background colors using functions (t/set-fg-color) and (t/set-bg-color)
Available colors are: :black :white :red :green :blue :cyan :magenta :yellow :default 

```clojure
(let [tm (t/unix-terminal)]
    (t/enter-private-mode tm)
    (t/set-fg-color tm :yellow)
    (t/set-bg-color tm :blue)
    (t/put-string tm "This is color string.")
    (read-line)
    (t/exit-private-mode tm))
```

![image of print-chars](https://github.com/middlesphere/clj-terminal/blob/master/examples/resources/colors.png)

## License

Copyright © 2016-2017 by Mikhail Ananyev

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
