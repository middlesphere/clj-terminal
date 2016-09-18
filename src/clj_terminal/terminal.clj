(ns clj-terminal.terminal
  (:require [clj-terminal.terminal-spec :as ts])
  (:import (com.googlecode.lanterna.terminal.ansi UnixTerminal)
           (com.googlecode.lanterna TextColor$ANSI SGR)
           (com.googlecode.lanterna.terminal Terminal)
           (com.googlecode.lanterna.input KeyStroke)))


(defn unix-terminal
  "create new unix terminal object."
  []
  (UnixTerminal.))

(defn enter-private-mode
  "private mode where supported, give your terminal a private area to use, separate from what
  was there before. It will preserve the terminal history and restore it when you exit private mode.
  also it will disable the scrolling and leave you with a fixed screen."
  [^Terminal t]
  (.enterPrivateMode t))

(defn exit-private-mode
  "If you have previously entered private mode, this method will exit this and, depending on
  implementation, maybe restore what the terminal looked like before private mode was entered."
  [^Terminal t]
  (.exitPrivateMode t))

(defn get-cursor-position
  "get current cusror position on terminal"
  [^Terminal t]
  (let [p (.getCursorPosition t)]
    {:col (.getColumn p)
     :row (.getRow p)}))

(defn set-cursor-position
  "move cursor to a new postion on terminal"
  ([^Terminal t col row]
   (.setCursorPosition t col row))
  ([^Terminal t coord]
   (.setCursorPosition t (:col coord) (:row coord))))

(defn clear
  "clear terminal screen."
  [^Terminal t]
  (.clearScreen t))

(defn- ansi-color
  "return ansi color as TextColor object"
  [c]
  (case c
    :black TextColor$ANSI/BLACK
    :white TextColor$ANSI/WHITE
    :red TextColor$ANSI/RED
    :green TextColor$ANSI/GREEN
    :blue TextColor$ANSI/BLUE
    :cyan TextColor$ANSI/CYAN
    :magenta TextColor$ANSI/MAGENTA
    :yellow TextColor$ANSI/YELLOW
    :default TextColor$ANSI/DEFAULT))

(defn set-bg-color
  "set background color for terminal"
  [^Terminal t c]
  (.setBackgroundColor t (ansi-color c)))

(defn set-fg-color
  "set foreground color for terminal"
  [^Terminal t c]
  (.setForegroundColor t (ansi-color c)))

(defn get-terminal-size
  "get current terminal size"
  [^Terminal t]
  (let [p (.getTerminalSize t)]
    {:col (.getColumns p)
     :row (.getRows p)}))

(defn put-character
  "put character on terminal.
  if column and row are present then put character to terminal using given coordinates."
  ([^Terminal t c]
   (.putCharacter t c))
  ([^Terminal t c col row]
   (set-cursor-position t col row)
   (put-character t c)))

(defn put-string
  "put string on terminal.
  if column and row are present then put string to terminal using given coordinates."
  ([^Terminal t s]
   (dorun (map (partial put-character t) s)))
  ([t s col row]
   (set-cursor-position t col row)
   (put-string t s)))

(defn visible-cursor
  "enable or disable cursor on terminal"
  [^Terminal t b]
  (.setCursorVisible t b))

(defn- key-types
  "decode kestroke"
  [^KeyStroke k]
  (let [t (.getKeyType k)
        cas {:ctrl  (.isCtrlDown k)
             :alt   (.isAltDown k)
             :shift (.isShiftDown k)}
        v (case (str t)
            "ArrowDown" :arrow-down
            "ArrowLeft" :arrow-left
            "ArrowRight" :arrow-right
            "ArrowUp" :arrow-up
            "Backspace" :backspace
            "Character" :character
            "Delete" :delete
            "End" :end
            "Enter" :enter
            "EOF" :eof
            "Escape" :escape
            "F1" :f1
            "F2" :f2
            "F3" :f3
            "F4" :f4
            "F5" :f5
            "F6" :f6
            "F7" :f7
            "F8" :f8
            "F9" :f9
            "F10" :f10
            "F11" :f11
            "F12" :f12
            "Home" :home
            "Insert" :insert
            "PageDown" :page-down
            "PageUp" :page-up
            "ReverseTab" :reverse-tab
            "Tab" :tab
            :unknown)
        result (if (= v :character)
                 {:value (.charValue ^Character (.getCharacter k))
                  :type  :character}
                 {:value v
                  :type  :special})]
    (merge result cas)))

(defn read-input
  "read key from keyboard and return decoded value as map. blocking until key pressed."
  [^Terminal t]
  (let [k (.readInput t)]
    (key-types k)))

(defn poll-input
  "read key from keyboard and return decoded value as map. non-blocking operation.
   returns null immediately if there is nothing on the input stream"
  [^Terminal t]
  (let [k (.pollInput t)]
    (if k
      (key-types k)
      {:value :empty
       :type  :special})))


(defn bell
  "Prints 0x7 to the terminal, which will make the terminal (emulator) ring
  a bell (or more likely beep)."
  [^Terminal t]
  (.bell t))

(defn maximize
  "Maximizes the terminal, so that it takes up all available space"
  [^Terminal t]
  (.maximize t))

(defn unmaximize
  "Restores the terminal back to its previous size, after having been maximized"
  [^Terminal t]
  (.unmaximize t))

(defn new-size
  "set terminal size"
  [^Terminal t col row]
  (.setTerminalSize t col row))

(defn text-effect
  "Activates/deactivates various text effects.
  modifies a state inside the terminal that will apply to all characters written afterwards,
  such as bold, italic, blinking code and so on."
  [^Terminal t eff enable?]
  (let [term-eff (case eff
                   :blink SGR/BLINK                         ;;works +/-
                   :bold SGR/BOLD                           ;;works +/-
                   :bordered SGR/BORDERED
                   :circled SGR/CIRCLED
                   :crossed SGR/CROSSED_OUT
                   :fraktur SGR/FRAKTUR
                   :reverse SGR/REVERSE                     ;;works
                   :underline SGR/UNDERLINE)]               ;;works
    (if enable?
      (.enableSGR t term-eff)
      (.disableSGR t term-eff))))


(defn reset-color-eff
  "Removes all currently active text effects and sets foreground and background colors back to default."
  [^Terminal t]
  (.resetColorAndSGR t))


