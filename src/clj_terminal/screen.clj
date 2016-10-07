(ns clj-terminal.screen
  (:gen-class)
  (:require [clj-terminal.screen-spec :as ss]
            [clj-terminal.terminal :as t])
  (:import (com.googlecode.lanterna.screen TerminalScreen Screen$RefreshType)
           (com.googlecode.lanterna.terminal Terminal DefaultTerminalFactory)
           (com.googlecode.lanterna TerminalPosition TerminalSize TextCharacter TextColor TextColor$ANSI SGR)
           (com.googlecode.lanterna.graphics TextGraphics)))

(defn- default-terminal
  "create new default terminal object."
  []
  (.createTerminal (DefaultTerminalFactory.)))

(defn new-screen
  "create new screen"
  ([] (TerminalScreen. (default-terminal)))
  ([^Terminal tm] (TerminalScreen. tm)))

(defn start
  "start screen to make it visible. calling this fn will make the underlying terminal work in private mode."
  [^TerminalScreen ts]
  (.startScreen ts))

(defn stop
  "stop screen. calling this fn will make the underlying terminal
  leave private mode, effectively going back to whatever state the terminal
  was in before calling start fn."
  [^TerminalScreen ts]
  (.stopScreen ts))

(defn refresh
  "refresh screen to make visible some buffered changes.
   :delta - will calculate a diff between the back-buffer and the front-buffer
   :auto - will make a guess at which refresh type would be the fastest and use this one.
   :full - will send a clear command to the terminal, then redraw the whole back-buffer line by line"
  ([^TerminalScreen ts]
   (.refresh ts))
  ([^TerminalScreen ts type]
   (let [t (case type
             :delta Screen$RefreshType/DELTA
             :auto Screen$RefreshType/AUTOMATIC
             :full Screen$RefreshType/COMPLETE
             Screen$RefreshType/COMPLETE)]
     (.refresh ts t))))

(defn get-terminal
  "return the underlying terminal that this screen is using."
  [^TerminalScreen ts]
  (.getTerminal ts))

(defn resize-if-necessary
  "resize screen and adjust internal buffers if terminal size was changed.
  return the new terminal size if the terminal has been resized since last call of this fn or nil."
  [^TerminalScreen ts]
  (let [r (.doResizeIfNecessary ts)]
    (when r
      {:col (.getColumns r)
       :row (.getRows r)})))

(defn scroll-lines
  "scroll lines and refresh the screen.
  first-line - first line of the range to be scrolled (top line is 0)
  last-ine - last (inclusive) line of the range to be scrolled
  distance - if > 0: move lines up, else if < 0: move lines down.
  buffered operation, need refresh screen to make changes visible."
  [^TerminalScreen ts first-line last-line distance]
  (.scrollLines ts first-line last-line distance))

(defn- text-effect
  "activates/deactivates various text effects such as bold, italic, blinking code and so on.
  modifies a state inside current text graphics object only.  text graphics object is internal screen object."
  [^TextGraphics tg effkwd-vec enable?]
  (let [kwd->eff (fn [effkwd]
                   (case effkwd
                     :blink SGR/BLINK                       ;;works +/-
                     :bold SGR/BOLD                         ;;works +/-
                     :bordered SGR/BORDERED
                     :circled SGR/CIRCLED
                     :crossed SGR/CROSSED_OUT
                     :fraktur SGR/FRAKTUR
                     :reverse SGR/REVERSE                   ;;works
                     :underline SGR/UNDERLINE))             ;;works
        eff-vec (mapv kwd->eff effkwd-vec)]
    (if enable?
      (.enableModifiers tg (into-array SGR eff-vec))
      (.disableModifiers tg (into-array SGR eff-vec)))))

(defn rectangle
  "draw a rectangle using a particular character as a border, coordinates and colors.
  buffered operation, need refresh screen to make changes visible."
  [^TerminalScreen ts col row width height c & colors]
  (let [tg (.newTextGraphics ts)]
    (if colors
      (do
        (when (:eff (first colors))
          (text-effect tg (:eff (first colors)) true))
        (.drawRectangle tg (TerminalPosition. col row) (TerminalSize. width height)
                        (TextCharacter. c
                                        (t/ansi-color (:fg (first colors)))
                                        (t/ansi-color (:bg (first colors)))
                                        (.getActiveModifiers tg))))
      (.drawRectangle tg (TerminalPosition. col row) (TerminalSize. width height) c))))

(defn fill-rectangle
  "fill a rectangle with a particular character, using given coordinates and colors.
  buffered operation, need refresh screen to make changes visible."
  [^TerminalScreen ts col row width height c & colors]
  (let [tg (.newTextGraphics ts)]
    (if colors
      (do
        (when (:eff (first colors))
          (text-effect tg (:eff (first colors)) true))
        (.fillRectangle tg (TerminalPosition. col row) (TerminalSize. width height)
                        (TextCharacter. c
                                        (t/ansi-color (:fg (first colors)))
                                        (t/ansi-color (:bg (first colors)))
                                        (.getActiveModifiers tg))))
      (.fillRectangle tg (TerminalPosition. col row) (TerminalSize. width height) c))))

(defn put-character
  "put character on screen using given coordinates and colors.
  buffered operation, need refresh screen to make changes visible."
  [^TerminalScreen ts c col row & colors]
  (let [tg (.newTextGraphics ts)]
    (when colors
      (.setForegroundColor tg (t/ansi-color (:fg (first colors))))
      (.setBackgroundColor tg (t/ansi-color (:bg (first colors))))
      (when (:eff (first colors))
        (text-effect tg (:eff (first colors)) true)))
    (.setCharacter tg col row c)))


(defn put-string
  "put string on screen using given coordinates and colors.
  buffered operation, need refresh screen to make changes visible."
  [^TerminalScreen ts s col row & colors]
  (let [tg (.newTextGraphics ts)]
    (when colors
      (.setForegroundColor tg (t/ansi-color (:fg (first colors))))
      (.setBackgroundColor tg (t/ansi-color (:bg (first colors))))
      (when (:eff (first colors))
        (text-effect tg (:eff (first colors)) true)))
    (.putString tg col row s)))

(defn line
  "draw line using given char, coordinates and colors.
  buffered operation, need refresh screen to make changes visible."
  [^TerminalScreen ts from-col from-row to-col to-row c & colors]
  (let [tg (.newTextGraphics ts)]
    (if colors
      (do
        (when (:eff (first colors))
          (text-effect tg (:eff (first colors)) true))
        (.drawLine tg from-col from-row to-col to-row
                   (TextCharacter. c
                                   (t/ansi-color (:fg (first colors)))
                                   (t/ansi-color (:bg (first colors)))
                                   (.getActiveModifiers tg))))
      (.drawLine tg from-col from-row to-col to-row c))))
