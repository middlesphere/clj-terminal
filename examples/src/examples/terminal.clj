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