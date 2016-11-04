(ns clj-terminal.gui
  (:gen-class)
  (:require [clj-terminal.screen :as s]
            [clj-terminal.terminal :as t]
            [clojure.java.io :refer (reader resource)])
  (:import (com.googlecode.lanterna TextColor$ANSI TextColor TerminalSize SGR)
           (com.googlecode.lanterna.gui2 EmptySpace DefaultWindowManager MultiWindowTextGUI Panel
                                         LayoutManager AbsoluteLayout GridLayout LinearLayout Direction BorderLayout
                                         Component Borders Label BasicWindow Window$Hint Window TextBox TextBox$Style)
           (com.googlecode.lanterna.screen TerminalScreen)
           (com.googlecode.lanterna.graphics Theme SimpleTheme PropertyTheme)
           (com.googlecode.lanterna.bundle LanternaThemes)
           (java.util Properties Arrays)))

(defn empty-space
  "simple component which draws a solid color over its area"
  ([]                                                       ;EmptySpace: size 1x1, default color chosen from the theme
   (EmptySpace.))
  ([bg-color & params]                                      ;EmptySpace with a specified color and size
   (if params
     (if bg-color
       (EmptySpace. ^TextColor (t/ansi-color bg-color) (TerminalSize. (:cols (first params))
                                                                      (:rows (first params))))
       (EmptySpace. (TerminalSize. (:cols (first params))   ;pick color from the theme
                                   (:rows (first params)))))
     (EmptySpace. ^TextColor (t/ansi-color bg-color)))))

(defn bordered
  "takes component and wrap it to another component with border
  return new component wrapped with border."
  [c border-type title]
  (let [b (case border-type
            :single `Borders/singleLine
            :double `Borders/doubleLine
            :single-bevel `Borders/singleLineBevel
            :double-bevel `Borders/doubleLineBevel
            :single-reverse-bevel `Borders/singleLineReverseBevel
            :double-reverse-bevel `Borders/doubleLineReverseBevel)
        b2 (if title
             (eval (list b title))
             (eval (list b)))]
    (.withBorder c b2)))

(defn text-gui
  ([] (let [ts (s/new-screen)]
        (MultiWindowTextGUI. ts (DefaultWindowManager.) (empty-space :black))))
  ([^TerminalScreen ts bg-color]
   (MultiWindowTextGUI. ts (DefaultWindowManager.) (empty-space bg-color))))

(defn panel
  "create and return new Panel object"
  []
  (Panel.))

(defn- direction
  "convert keyword to Direction object"
  [d]
  (case d
    :v Direction/VERTICAL
    :h Direction/HORIZONTAL
    Direction/VERTICAL))

(defn panel-layout
  "set panel layout"
  [^Panel p params]
  (case (:type params)
    :absolute (.setLayoutManager p (AbsoluteLayout.))
    :grid (.setLayoutManager p (GridLayout. (:cols params))) ;:cols - number of columns
    :linear (.setLayoutManager p (LinearLayout. (direction
                                                  (:dir params)))) ; :v - vertical :h - horizontal
    :border (.setLayoutManager p (BorderLayout.))))

(defn panel-add
  "add component to panel"
  [^Panel p ^Component c]
  (.addComponent p c))

(defn window-add
  "set component to window"
  [^Window w ^Component c]
  (.setComponent w c))

(defn label
  "create text label using given parameteres."
  [text & params]
  (let [l (Label. text)]
    (when params
      (when-let [c (:fg (first params))]
        (.setForegroundColor l (t/ansi-color c)))
      (when-let [c (:bg (first params))]
        (.setBackgroundColor l (t/ansi-color c)))
      (when-let [w (:max-width (first params))]
        (.setLabelWidth l (Integer. ^int w))))
    l))

(defn lb-set-text
  "update the text this label is displaying"
  [^Label l text]
  (.setText l text))

(defn lb-get-text
  "return the text this label is displaying"
  [^Label l text]
  (.getText l))

(defn set-simple-theme
  "set simple theme for particular component using foreground and background colors."
  [^Component c params]
  (.setTheme c (SimpleTheme. (t/ansi-color (:fg params))
                             (t/ansi-color (:bg params))
                             (into-array SGR []))))

(defn set-theme
  "set named theme for particular component."
  [c ^String theme-name]
  (.setTheme c (LanternaThemes/getRegisteredTheme theme-name)))

(defn load-theme
  "load theme from properties file and register it with new name in registry.
  to set loaded theme for components use named-theme fn and new name."
  [file-name new-name]
  (let [props (Properties.)]
    (.load props (reader (resource file-name)))
    (LanternaThemes/registerTheme new-name (PropertyTheme. props))))

(defn- k->hint
  [hint]
  (case hint
    :centered Window$Hint/CENTERED
    :expanded Window$Hint/EXPANDED
    :fit-terminal Window$Hint/FIT_TERMINAL_WINDOW
    :fixed-pos Window$Hint/FIXED_POSITION
    :fixed-size Window$Hint/FIXED_SIZE
    :full-screen Window$Hint/FULL_SCREEN
    :modal Window$Hint/MODAL
    :no-decor Window$Hint/NO_DECORATIONS
    :no-focus Window$Hint/NO_FOCUS
    :no-post-render Window$Hint/NO_POST_RENDERING))

(defn window
  "create window"
  ([]
   (BasicWindow.))
  ([^String title]
    (BasicWindow. title))
  ([^String title params]
    (let [w (BasicWindow. title)]
      (when (:esc-close? params)
        (.setCloseWindowWithEscape w true)))))

(defn window-hints
  "set window hints"
  [w hints-vec]
  (.setHints w (Arrays/asList (into-array (mapv #(k->hint %) hints-vec)))))

(defn- k->text-style
  [s]
  (case s
    :multi-line TextBox$Style/MULTI_LINE
    :single-line TextBox$Style/SINGLE_LINE))

(defn text-box
  "return new text box component that keeps a text content that is editable by the user."
  ([]
   (TextBox.))
  ([^String init-text]
    (TextBox. init-text))
  ([^String init-text {:keys [style mask size valid-pattern]}]
   (let [t (if size
             (if style
               (TextBox. (TerminalSize. (:col size) (:row size)) init-text (k->text-style style))
               (TextBox. (TerminalSize. (:col size) (:row size))))
             (if style
               (TextBox. init-text ^TextBox$Style (k->text-style style))
               (TextBox. init-text)))]
     (when mask
       (.setMask t (Character. mask)))
     (when valid-pattern
       (.setValidationPattern t (re-pattern valid-pattern)))
     t)))

(defn tb-get-text
  "returns the text from text box, for multi-line mode all lines will be concatenated together with \n as separator."
  [^TextBox t]
  (.getText t))

(defn tb-set-text
  "updates the text content of the text box to the supplied string."
  [^TextBox t text]
  (.setText t text))

(defn tb-line-count
  "returns the number of lines currently in this TextBox."
  [^TextBox t]
  (.getLineCount t))

(defn tb-get-caret-pos
  "returns the position of the caret, as a {:col :row} where the row and columns equals the coordinates
  in a multi-line TextBox and for single-line TextBox you can ignore the row component."
  [^TextBox t]
  (let [p (.getCaretPosition t)]
    {:row (.getRow p)
     :col (.getColumn p)}))

(defn tb-set-caret-pos
  "move the text caret position to a new position"
  ([^TextBox t col]
   (.setCaretPosition t col))
  ([^TextBox t col row]
   (.setCaretPosition t col row)))

(defn tb-read-only
  "enable/disable read-only mode of the text box."
  [^TextBox t enable?]
  (.setReadOnly t enable?))

(defn tb-warp-mode
  "enable/disable caret warp mode of the text box.
  Sets if the caret should jump to the beginning of the next line
  if right arrow is pressed while at the end of a line."
  [^TextBox t enable?]
  (.setCaretWarp t enable?))

(defn tb-horizon-switch
  "enable/disable horizontal focus switching of the text box.
  If set to true, the TextBox will switch focus to the next available component to the left
  if the cursor in the TextBox is at the left-most position (index 0) on the row and the user pressed the 'left'
  arrow key, or vice versa for pressing the 'right' arrow key when the cursor in at the right-most
  position of the current row."
  [^TextBox t enable?]
  (.setHorizontalFocusSwitching t enable?))


(defn tb-vertical-switch
  "enable/disable vertical focus switching of the text box.
  If set to true, the component will switch to the next available component above if the cursor is at the top
  of the TextBox and the user presses the 'up' array key, or switch to the next available component below if the
  cursor is at the bottom of the TextBox and the user presses the 'down' array key."
  [^TextBox t enable?]
  (.setVerticalFocusSwitching t enable?))