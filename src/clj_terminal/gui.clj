(ns clj-terminal.gui
  (:gen-class)
  (:require [clj-terminal.screen :as s]
            [clj-terminal.terminal :as t]
            [clojure.java.io :refer (reader resource)])
  (:import (com.googlecode.lanterna TextColor$ANSI TextColor TerminalSize SGR)
           (com.googlecode.lanterna.gui2 EmptySpace DefaultWindowManager MultiWindowTextGUI Panel
                                         LayoutManager AbsoluteLayout GridLayout LinearLayout Direction BorderLayout
                                         Component Borders Label BasicWindow Window$Hint Window TextBox TextBox$Style
                                         Button ComboBox ComboBox$Listener CheckBox$Listener CheckBox CheckBoxList
                                         CheckBoxList$Listener Separator AbstractInteractableComponent RadioBoxList
                                         RadioBoxList$Listener ActionListBox ProgressBar)
           (com.googlecode.lanterna.screen TerminalScreen)
           (com.googlecode.lanterna.graphics Theme SimpleTheme PropertyTheme)
           (com.googlecode.lanterna.bundle LanternaThemes)
           (java.util Properties Arrays Collection)
           (clojure.lang PersistentVector)
           (com.googlecode.lanterna.gui2.dialogs MessageDialogBuilder MessageDialogButton TextInputDialogBuilder
                                                 FileDialogBuilder ActionListDialogBuilder ListSelectDialogBuilder)
           (java.util.regex Pattern)
           (java.io File)
           (com.googlecode.lanterna.gui2.table Table)))

(defn set-enabled
  "Enable/disable component"
  [^AbstractInteractableComponent c enabled?]
  (.setEnabled c enabled?))

(defn is-enabled?
  "return true if component enabled or false if disabled"
  [^AbstractInteractableComponent c]
  (.isEnabled c))


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

(defn gui-add-window
  "add window to given ^MultiWindowTextGUI object"
  [^MultiWindowTextGUI gui ^Window w]
  (.addWindow gui w))

(defn gui-add-window-and-wait
  "add window to given ^MultiWindowTextGUI object and wait before window closes."
  [^MultiWindowTextGUI gui ^Window w]
  (.addWindowAndWait gui w))

(defn gui-set-active-window
  "set active window for given ^MultiWindowTextGUI object"
  [^MultiWindowTextGUI gui ^Window w]
  (.setActiveWindow gui w))

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

(defn progress-bar
  "Creates a new progress bar with a defined range of minimum to maximum.
  preferred-width - size hint, in number of columns, for this progress bar,
  0 or less means that there is no hint."
  [^long min ^long max ^long preferred-width]
  (ProgressBar. min max preferred-width))

(defn pb-get-progress
  "Returns the current progress bar's value from minimum
  to maximum, expressed as a float from 0.0f to 1.0f."
  [^ProgressBar pb]
  (.getProgress pb))

(defn pb-get-value
  "Returns the current value of this progress bar,
  which represents how complete the progress indication is."
  [^ProgressBar pb]
  (.getValue pb))

(defn pb-get-min
  "Returns the current minimum value for this progress bar"
  [^ProgressBar pb]
  (.getMin pb))

(defn pb-get-max
  "Returns the current maximum value for this progress bar"
  [^ProgressBar pb]
  (.getMax pb))

(defn pb-get-pref-width
  "Returns the preferred width of the progress bar component, in number of columns."
  [^ProgressBar pb]
  (.getPreferredWidth pb))

(defn pb-get-label-format
  "Returns the current label format string which is the template for what the
  progress bar would like to be the label printed."
  [^ProgressBar pb]
  (.getLabelFormat pb))

(defn pb-set-value
  "Updates the value of this progress bar, which will update the visual state."
  [^ProgressBar pb ^long new-value]
  (.setValue pb new-value))

(defn pb-set-min
  "Updates the minimum value of this progress bar."
  [^ProgressBar pb ^long min]
  (.setMin pb min))

(defn pb-set-max
  "Updates the maximum value of this progress bar."
  [^ProgressBar pb ^long max]
  (.setMax pb max))

(defn pb-set-pref-width
  "Updated the preferred width hint, which tells the
  renderer how wide this progress bar would like to be."
  [^ProgressBar pb ^long preferred-width]
  (.setPreferredWidth pb preferred-width))

(defn pb-set-label-format
  "Sets the label format this progress bar should use when the component is drawn."
  [^ProgressBar pb ^String label-format]
  (.setLabelFormat pb label-format))

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

(defn registered-themes
  "return vector of strings with registered themes"
  []
  (into [] (LanternaThemes/getRegisteredThemes)))


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
       (.setCloseWindowWithEscape w true)
       w))))

(defn win-set-close-with-escape?
  "enable close ^Window with Escape key"
  [^Window w ^Boolean enable?]
  (.setCloseWindowWithEscape w enable?))

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

(defn button
  "Creates a new button with a specific label and (if provided) an associated action to fire when triggered by the user
  fn-action - function with no args"
  [^String label & [fn-action]]
  (if fn-action
    (Button. label (reify Runnable (run [this] (fn-action))))
    (Button. label)))

(defn bt-set-label
  "set new label for button"
  [^Button bt ^String new-label]
  (.setLabel bt new-label))

(defn bt-get-label
  "get current label for button"
  [^Button bt]
  (.getLabel bt))

(defn combo-box
  "create ComboBox that allows the user to select one out of multiple items through a drop-down menu."
  [^Collection vec-items]
  (ComboBox. vec-items))

(defn cb-add-item
  "add item to ComboBox"
  ([^ComboBox cb item]
   (.addItem cb item))
  ([^ComboBox cb ^long index item]
   (.addItem cb index item)))

(defn cb-get-text
  "Returns the text currently displayed in the combo box, this will likely be the label of the selected item
  but for writable combo boxes it's also what the user has typed in"
  [^ComboBox cb]
  (.getText cb))

(defn cb-get-item
  "Returns the item at the specific index"
  [^ComboBox cb ^long index]
  (.getItem cb index))

(defn cb-get-selected-item
  "Returns the item at the selected index"
  [^ComboBox cb]
  (.getSelectedItem cb))

(defn cb-get-selected-index
  "Returns the index of the currently selected item"
  [^ComboBox cb]
  (.getSelectedIndex cb))

(defn cb-get-item-count
  "Counts and returns the number of items in this combo box"
  [^ComboBox cb]
  (.getItemCount cb))

(defn cb-set-selected-index
  "Programmatically selects one item in the combo box, which causes the displayed text
  to change to match the label of the selected index"
  [^ComboBox cb ^long index]
  (.setSelectedIndex cb index))

(defn cb-set-readonly
  "Sets the combo box to either read-only or writable."
  [^ComboBox cb ^Boolean read-only?]
  (.setReadOnly cb read-only?))

(defn cb-set-width
  "set combobox width"
  [^ComboBox cb ^long width]
  (.setPreferredSize cb (TerminalSize. width 1)))

(defn cb-clear-items
  "Removes all items from the combo box"
  [^ComboBox cb]
  (.clearItems cb))

(defn cb-listener
  "create listener for combobox.
  f is function with two args: ^int selectedIndex, ^int previousSelection
  return ComboBox$Listener object"
  [f]
  (reify ComboBox$Listener
    (onSelectionChanged [this current-index prev-index]
      (f current-index prev-index))))

(defn cb-add-listener
  "Adds a new listener to the ComboBox that will be called on every time when user select new item"
  [^ComboBox cb ^ComboBox$Listener listener]
  (.addListener cb listener))

(defn cb-remove-listener
  "Removes a listener from this ComboBox so that if it had been added earlier, it will no longer be called on user actions"
  [^ComboBox cb ^ComboBox$Listener listener]
  (.removeListener cb listener))

(defn check-box
  "Creates a new checkbox with a specific label, initially set to un-checked"
  [^String label]
  (CheckBox. label))

(defn chb-is-checked?
  "Returns the checked state of this check box"
  [^CheckBox chb]
  (.isChecked chb))

(defn chb-set-checked
  "Programmatically update the check box to a particular checked state"
  [^CheckBox chb ^Boolean checked?]
  (.setChecked chb checked?))

(defn chb-get-label
  "Returns the label of check box"
  [^CheckBox chb]
  (.getLabel chb))

(defn chb-set-label
  "Updates the label of the checkbox"
  [^CheckBox chb ^String label]
  (.setLabel chb label))

(defn chb-listener
  "create listener for checkbox.
  f is function with one arg: ^Boolean checked
  f is fired every time when the user has altered the checked state return CheckBox.Listener object"
  [f]
  (reify CheckBox$Listener
    (onStatusChanged [this checked?]
      (f checked?))))

(defn chb-add-listener
  "Adds a new listener to the Checkbox that will be called every time when the user has altered the checked state"
  [^CheckBox chb ^CheckBox$Listener listener]
  (.addListener chb listener))

(defn chb-remove-listener
  "Removes a listener from this Checkbox so that if it had been added earlier, it will no longer be called on user actions"
  [^CheckBox chb ^CheckBox$Listener listener]
  (.removeListener chb listener))

(defn chb-set-width
  "set checkbox width"
  [^CheckBox chb ^long width]
  (.setPreferredSize chb (TerminalSize. width 1)))

(defn check-box-list
  "Creates a new CheckBoxList that is initially empty and (if provided) set it to a pre-defined width and height.
  return CheckBoxList object"
  [& [width height]]
  (if (and width height)
    (CheckBoxList. (TerminalSize. width height))
    (CheckBoxList.)))

(defn chl-add-item
  "Adds an item to the checkbox list with (if provided) an explicit checked status
  Item can be any Java object (^Object), primarily Strings"
  [^CheckBoxList chl item & [checked?]]
  (if checked?
    (.addItem chl item checked?)
    (.addItem chl item)))

(defn chl-get-checked
  "Returns all the items in the list box that have checked state, as a vector"
  [^CheckBoxList chl]
  (into [] (.getCheckedItems chl)))

(defn chl-clear
  "Removes all items from the list box"
  [^CheckBoxList chl]
  (.clearItems chl))

(defn chl-is-checked-index?
  "return boolean state of particular item in check box list."
  [^CheckBoxList chl ^long index]
  (.isChecked chl index))

(defn chl-is-checked-item?
  "return boolean state of particular item in check box list."
  [^CheckBoxList chl ^Object item]
  (.isChecked chl item))

(defn chl-set-checked-item
  "Programmatically sets the checked state of an item in the list box
  item - object in CheckBoxList"
  [^CheckBoxList chl item checked?]
  (.setChecked chl item checked?))

(defn chl-get-item-count
  "Counts and returns the number of items in CheckBoxList"
  [^CheckBoxList chl]
  (.getItemCount chl))

(defn chl-listener
  "create listener for CheckBoxList.
  f is function with 2 args: ^int itemIndex, ^Boolean checked?
  f is called every time when user changes the toggle state of one item
  return CheckBoxList.Listener object"
  [f]
  (reify CheckBoxList$Listener
    (onStatusChanged [this index checked?]
      (f index checked?))))

(defn chl-add-listener
  "Adds a new listener to the CheckBoxList that will be called every time when user changes the toggle state of one item"
  [^CheckBoxList chl ^CheckBoxList$Listener listener]
  (.addListener chl listener))

(defn chl-remove-listener
  "Removes a listener from this CheckBoxList so that if it had been added earlier, it will no longer be called on user actions"
  [^CheckBoxList chl ^CheckBoxList$Listener listener]
  (.removeListener chl listener))

(defn separator
  "Separator for a specific direction. D is a keyword :h or :v."
  [d & [size]]
  (if size
    (.setPreferredSize (Separator. (direction d)) (TerminalSize. size 1))
    (Separator. (direction d))))

(defn radio-box-list
  "Creates a new RadioBoxList that is initially empty and (if provided) set it to a pre-defined width and height.
  return RadioBoxList object"
  [& [width height]]
  (if (and width height)
    (RadioBoxList. (TerminalSize. width height))
    (RadioBoxList.)))

(defn rbl-add-item
  "Adds an item to the RadioBoxList with (if provided) an explicit checked status
  Item can be any Java object (^Object), primarily Strings"
  [^RadioBoxList rbl item]
  (.addItem rbl item))

(defn rbl-get-checked
  "Returns item in the RadioBoxList that have checked state"
  [^RadioBoxList rbl]
  (.getCheckedItem rbl))

(defn rbl-clear
  "Removes all items from the RadioBoxList"
  [^RadioBoxList rbl]
  (.clearItems rbl))

(defn rbl-clear-selection
  "Un-checks the currently checked item (if any) and leaves the radiobox in a state where no item is checked."
  [^RadioBoxList rbl]
  (.clearSelection rbl))

(defn rbl-is-checked-index?
  "return boolean state of particular item in RadioBoxList."
  [^RadioBoxList rbl ^long index]
  (.isChecked rbl index))

(defn rbl-is-checked-item?
  "return boolean state of particular item in RadioBoxList."
  [^RadioBoxList rbl ^Object item]
  (.isChecked rbl item))

(defn rbl-set-checked-item
  "Programmatically sets the checked state of an item in the RadioBoxList
  item - object in RadioBoxList"
  [^RadioBoxList rbl item]
  (.setCheckedItem rbl item))

(defn rbl-set-checked-index
  "Programmatically sets the checked state of an item by index in the RadioBoxList"
  [^RadioBoxList rbl ^long index]
  (.setCheckedItemIndex rbl index))

(defn rbl-get-item-count
  "Counts and returns the number of items in RadioBoxList"
  [^RadioBoxList rbl]
  (.getItemCount rbl))

(defn rbl-listener
  "create listener for RadioBoxList.
  f is function with 2 args: ^int selected-index, ^int prev-selection
  f is called every time when the user changes which item is selected
  return RadioBoxList.Listener object"
  [f]
  (reify RadioBoxList$Listener
    (onSelectionChanged [this selected-index prev-selection]
      (f selected-index prev-selection))))

(defn rbl-add-listener
  "Adds a new listener to the RadioBoxList that will be called every time when user changes the toggle state of one item"
  [^RadioBoxList rbl ^RadioBoxList$Listener listener]
  (.addListener rbl listener))

(defn rbl-remove-listener
  "Removes a listener from this RadioBoxList so that if it had been added earlier, it will no longer be called on user actions"
  [^RadioBoxList rbl ^RadioBoxList$Listener listener]
  (.removeListener rbl listener))

(defn action-list-box
  "Creates a new ActionListBox that is initially empty and (if provided) set it to a pre-defined width and height.
  ActionListBox stores a list of actions the user can made. You can activate  actions by pressing the Enter or
  Space keys on the keyboard and the action associated with the currently selected item will fire.
  Each action runs within its own thread.
  return ActionListBox object"
  [& [width height]]
  (if (and width height)
    (ActionListBox. (TerminalSize. width height))
    (ActionListBox.)))

(defn alb-add-item
  "Adds a new item to the ActionListBox, which is displayed in the list using a supplied label.
  f - function which should be fired if item selected to activate."
  [^ActionListBox alb ^String label f]
  (.addItem alb label (reify Runnable
                        (run [this]
                          (f)))))

(defn alb-clear
  "Removes all items from the ActionListBox"
  [^ActionListBox alb]
  (.clearItems alb))

(defn alb-get-selected-item
  "Returns selected item in the ActionListBox"
  [^ActionListBox rbl]
  (.getSelectedItem rbl))

(defn alb-get-selected-index
  "Returns selected index in the ActionListBox"
  [^ActionListBox rbl]
  (.getSelectedIndex rbl))

(defn kw->button
  "convert keyword to MessageDialogButton object"
  [k]
  (case k
    :yes MessageDialogButton/Yes
    :no MessageDialogButton/No
    :abort MessageDialogButton/Abort
    :cancel MessageDialogButton/Cancel
    :close MessageDialogButton/Close
    :continue MessageDialogButton/Continue
    :ignore MessageDialogButton/Ignore
    :ok MessageDialogButton/OK
    :retry MessageDialogButton/Retry
    nil))

(defn button->kw
  "convert MessageDialogButton object to keyword"
  [b]
  (case (str b)
    "Yes" :yes
    "No" :no
    "Abort" :abort
    "Cancel" :cancel
    "Close" :close
    "Continue" :continue
    "Ignore" :ignore
    "OK" :ok
    "Retry" :retry
    nil))

(defn message-dialog
  "show message dialog using given gui object (^MultiWindowTextGUI).
  return: selected button value"
  [^MultiWindowTextGUI gui ^String title ^String message & buttons]
  (let [db (MessageDialogBuilder.)]
    (.setTitle db title)
    (.setText db message)
    (doseq [x buttons]
      (when-let [b (kw->button x)]
        (.addButton db b)))
    (let [d (.build db)
          result (.showDialog d gui)]
      (button->kw result))))

(defn text-input-dialog
  "show text input dialog using given gui object (^MultiWindowTextGUI).
  col, row and valid-regexp,valid-err-msg are optional.
  return: input text or nil if dialog canceled"
  [^MultiWindowTextGUI gui ^String title ^String init-text ^Boolean password-field? & [{:keys [col row
                                                                                               valid-regexp
                                                                                               valid-err-msg]}]]
  (let [tdb (TextInputDialogBuilder.)]
    (.setTitle tdb title)
    (.setInitialContent tdb init-text)
    (when password-field? (.setPasswordInput tdb true))
    (when (and col row) (.setTextBoxSize tdb (TerminalSize. col row)))
    (when (and valid-regexp valid-err-msg) (.setValidationPattern tdb (Pattern/compile valid-regexp) valid-err-msg))
    (let [td (.build tdb)
          result (.showDialog td gui)]
      result)))

(defn file-dialog
  "Dialog that allows the user to iterate the file system and pick file to open/save.
  return ^String full path for selected filename."
  [^MultiWindowTextGUI gui ^String title ^String description
   ^String action-label & [{:keys [selected-filename show-hidden-dirs?]}]]
  (let [fdb (FileDialogBuilder.)]
    (.setTitle fdb title)
    (.setDescription fdb description)
    (.setActionLabel fdb action-label)
    (when show-hidden-dirs? (.setShowHiddenDirectories fdb true))
    (when selected-filename (.setSelectedFile fdb (File. selected-filename)))
    (let [fd (.build fdb)
          result (.showDialog fd gui)]
      (.getAbsolutePath ^File result))))

(defn action-list-dialog
  "show dialog containing a multiple item action list box
  actions-vec is vector of maps where map structure is :label is ^String and :action is regular function w/o params.
  {:col :row} are optional (width and height).
  Example: [ {:label :action} {:label :action} ]
  return nil"
  [^MultiWindowTextGUI gui ^String title ^String description ^Boolean can-cancel? actions-vec & [{:keys [col row]}]]
  (let [adb (ActionListDialogBuilder.)]
    (.setTitle adb title)
    (.setDescription adb description)
    (.setCanCancel adb can-cancel?)
    (when (and col row) (.setListBoxSize adb (TerminalSize. col row)))
    (doseq [m actions-vec]
      (.addAction adb (:label m) (reify Runnable
                                   (run [this]
                                     ((:action m))))))
    (let [ad (.build adb)
          result (.showDialog ad gui)]
      result)))

(defn list-select-dialog
  "Dialog that allows the user to select an item from a list
  items-vec is vector of objects: Strings or objects with .toString method.
  {:col :row} are optional (width and height).
  return selected item"
  [^MultiWindowTextGUI gui ^String title ^String description ^Boolean can-cancel? items-vec & [{:keys [col row]}]]
  (let [ldb (ListSelectDialogBuilder.)]
    (.setTitle ldb title)
    (.setDescription ldb description)
    (.setCanCancel ldb can-cancel?)
    (when (and col row) (.setListBoxSize ldb (TerminalSize. col row)))
    (doseq [i items-vec]
      (.addListItem ldb i))
    (let [ld (.build ldb)
          result (.showDialog ld gui)]
      result)))

(defn table
  "Creates a new Table with the number of columns as specified by the array of labels"
  [header-vec]
  (Table. (into-array header-vec)))

(defn table-add-row
  "add row to the ^Table
  row-vec - is regular Clojure vector with number of items equals to header-vec items."
  [^Table t row-vec]
  (.addRow (.getTableModel t) row-vec))

(defn table-get-selected-column-index
  "Returns the currently selection column index, if in cell-selection mode."
  [^Table t]
  (.getSelectedColumn t))

(defn table-get-selected-row-index
  "Returns the index of the currently selected row"
  [^Table t]
  (.getSelectedRow t))

(defn table-cell-selection-mode
  "If true, the user will be able to select and navigate individual cells,
  otherwise the user can only select full rows."
  [^Table t ^Boolean enable?]
  (.setCellSelection t enable?))

(defn table-get-cell
  "Returns the cell value stored at a specific column/row coordinate."
  [^Table t ^long col ^long row]
  (.getCell (.getTableModel t) col row))

(defn table-get-row
  "Returns a row from the table as a list of the cell data"
  [^Table t ^long index]
  (into [] (.getRow (.getTableModel t) index)))

(defn table-get-row-count
  "Returns number of rows in the model"
  [^Table t]
  (.getRowCount (.getTableModel t)))

(defn table-get-column-count
  "Returns the number of columns in the model"
  [^Table t]
  (.getColumnCount (.getTableModel t)))

(defn table-get-rows
  "Returns all rows  as a list of lists containing the data as elements"
  [^Table t]
  (.getRows (.getTableModel t)))

(defn table-get-column-label
  "Returns the label of a column header"
  [^Table t ^long index]
  (.getColumnLabel (.getTableModel t) index))

(defn table-get-all-labels
  "Returns all column header label as a list of strings"
  [^Table t]
  (.getColumnLabels (.getTableModel t)))

(defn table-set-cell
  "Updates the call value stored at a specific column/row coordinate."
  [^Table t ^long col ^long row new-value]
  (.setCell (.getTableModel t) col row new-value))

(defn table-remove-row
  "Removes a row at a particular index from the table model"
  [^Table t ^long index]
  (.removeRow (.getTableModel t) index))

(defn table-remove-column
  "Removes a column from the table model"
  [^Table t ^long index]
  (.removeColumn (.getTableModel t) index))

(defn table-insert-row
  "Inserts a new row to the table model at a particular index.
  new-row-vec - regular Clojure vector with data, size of column count"
  [^Table t ^long index new-row-vec]
  (.insertRow (.getTableModel t) index new-row-vec))

(defn table-insert-column
  "Adds a new column into the table model at a specified index.
  new-col-vec - regular Clojure vector with data"
  [^Table t ^long index ^String new-col-label new-col-vec]
  (.insertColumn (.getTableModel t) index new-col-label (into-array new-col-vec)))

(defn table-set-visible-cols
  "Sets the number of columns this table should show."
  [^Table t ^long visible-cols]
  (.setVisibleColumns t visible-cols))

;THIS is not working!!

;(defn table-set-visible-cols
;  "Sets the number of columns this table should show.
;  If there are more columns in the table model, a scrollbar will be used to allow
;  the user to scroll left and right and view all columns."
;  [^Table t ^long visible-cols]
;  (.setVisibleColumns t visible-cols))

(defn table-set-visible-rows
  "Sets the number of rows this table will show. If there are more rows in the table model,
  a scrollbar will be used to allow the user to scroll up and down and view all rows."
  [^Table t ^long visible-rows]
  (.setVisibleRows t visible-rows))

(defn table-set-selected-row
  "Sets the index of the selected row and ensures the selected row is visible in the view"
  [^Table t ^long selected-row]
  (.setVisibleRows t selected-row))

(defn table-set-select-action
  "Assigns an action to run whenever the user presses the enter key while focused on the table.
  f - regular function with zero args"
  [^Table t f]
  (.setSelectAction t (reify Runnable (run [this] (f)))))