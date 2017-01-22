(ns examples.terminal
  (:gen-class)
  (:require [clj-terminal.terminal :as t]))


(defn basic-terminal
  []
  (let [tm (t/unix-terminal)]
    (t/set-fg-color tm :yellow)
    (t/put-string tm "this is basic terminal example\n")
    (t/set-fg-color tm :default)))                          ;restore default terminal colors

(defn private-mode
  []
  (let [tm (t/unix-terminal)]
    (t/enter-private-mode tm)
    (t/put-string tm "this is private mode demo\nPress ENTER to exit.")
    (read-line)
    (t/exit-private-mode tm)))

(defn print-chars
  []
  (let [tm (t/unix-terminal)]
    (t/put-character tm \H) (t/put-character tm \e) (t/put-character tm \l) (t/put-character tm \l)
    (t/put-character tm \o) (t/put-character tm \!)
    (t/put-string tm "\nPress ENTER to exit.\n")
    (read-line)))

(defn print-chars-pos
  []
  (let [tm (t/unix-terminal)]
    (t/enter-private-mode tm)

    (t/put-character tm \H 2 6) (t/put-character tm \e 2 5) (t/put-character tm \l 2 4) (t/put-character tm \l 2 3)
    (t/put-character tm \o 2 2) (t/put-character tm \! 2 1)

    (t/put-string tm "Press ENTER to exit." 4 3)

    (read-line)
    (t/exit-private-mode tm)))