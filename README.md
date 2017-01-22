Table of contents
===================

   * [Why we need a terminal? (IoT era is coming)](#why-we-need-a-terminal-iot-era-is-coming)
   * [About Lanterna](#about-lanterna)
   * [Intro](#intro)
   * [Terminal layer](#terminal-layer)
      * [Private mode](#private-mode)
      * [Writing text](#writing-text)
      * [Moving cursor](#moving-cursor)
      * [Colors (background and foreground)](#colors-background-and-foreground)
      * [Input](#input)
      * [Terminal size](#terminal-size)
      * [Other functions](#other-functions)
   * [Screen layer](#screen-layer)
   * [License](#license)

# Why we need a terminal? (IoT era is coming)

The GUI interfaces are not always available or suitable for interacting human <-> machine.  
Terminal is fundamentally simple interface available in any environment and suitable for various cases: 
system control, system configuration, backup/restore procedures or other manipulations with systems, 
databases, IoT devices, embeddable systems etc., where GUI interface is not available or broken.
If you build an information security system then terminal interface should be implemented before any GUI interfaces 
and should be always available, cause under some circumstances the terminal may be the only interface available for access to 
security system.
Internet of Things bring us a tons of devices where terminal may be the most suitable way for 
interacting human <-> machine.

# About Lanterna

clj-terminal is Clojure wrapper of Lanterna 3 Java library for creating text-based GUIs (https://github.com/mabe02/lanterna)

Lanterna is a Java library allowing you to write easy semi-graphical user interfaces in a text-only environment, 
very similar to the C library curses but with more functionality. Lanterna is supporting xterm compatible terminals 
and terminal emulators such as konsole, gnome-terminal, putty, xterm and many more. One of the main benefits of lanterna 
is that it's not dependent on any native library but runs 100% in pure Java.

Also, when running Lanterna on computers with a graphical environment (such as Windows or Xorg), a bundled terminal 
emulator written in Swing will be used rather than standard output. This way, you can develop as usual from your 
IDE (most of them doesn't support ANSI control characters in their output window) and then deploy to your headless 
server without changing any code.

# Intro

clj-terminal is structured into three layers each built on top of the other and you can easily choose which one 
fits your needs best:
 1. terminal (ns clj-terminal.terminal)
 2. screen (ns clj-terminal.screen)
 3. text gui (ns clj-terminal.gui)

# Terminal layer


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

## Private mode

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


## Writing text

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


## Moving cursor

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


## Colors (background and foreground)

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

In order to restore default terminal color run (t/set-fg-color tm :default).

## Input

There are two functions for user input: blocking and non-blocking. 
For blocking input use (t/read-input) which reads key from keyboard and return decoded value as map.
Example result for Ctrl-A {:value \a, :type :character, :ctrl true, :alt false, :shift false}}.

Function (t/read-input) blocks thread until key pressed. 
Here is example which reads user input and prints read value until Escape pressed:

```clojure
(let [tm (t/unix-terminal)]
    (t/enter-private-mode tm)
    (t/put-string tm "press any key or Escape for exit." 0 0)
    (loop [k (t/read-input tm)]
      (when (not= :escape (:value k))
        (t/put-string tm (str k) 0 1)
        (recur (t/read-input tm))))
    (t/exit-private-mode tm))
```
![image of print-chars](https://github.com/middlesphere/clj-terminal/blob/master/examples/resources/read-input.png)

For non-blocking user input use (t/poll-input) which also reads key from keyboard and return decoded value as map.
Function (t/poll-input) does not block thread and returns null immediately if there is nothing on the input stream.
If keyboard buffer contains some values poll-input will read them one by one.

## Terminal size

Terminal layer allows to control size of terminal. In order to get current terminal size use function (t/get-terminal-size)
If you need to maximize terminal screen use (t/maximize). To restore maximized terminal size use (t/unmaximize).
If you need particular terminal size then use (t/new-size).

## Other functions

 1. (t/clear) - clear terminal screen.
 2. (t/visible-cursor) - enable or disable cursor on terminal.
 3. (t/bell) - Prints 0x7 to the terminal, which will make the terminal (emulator) ring a bell (or more likely beep).
 4. (t/text-effect) - Activates/deactivates various text effects (blink, bold, reverse,underline etc.)
 5. (t/enable-mouse-capture-mode!) - enable catch mouse events in terminal. not all terminals are support this feature.

# Screen layer

Screen layer is most useful interface. Think of Screen as "double buffering for your console".
Screen acts as a buffer. You "draw" to the screen like you would normally draw directly to the terminal, 
but it doesn't appear to the user. When you're ready you tell the Screen to redraw. It will calculate the necessary 
changes and make them happen. This improves performance and makes it easy to avoid showing half-drawn UIs to your users.



# License

Copyright © 2016-2017 by Mikhail Ananyev

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
