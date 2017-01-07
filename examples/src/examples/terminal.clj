(ns examples.terminal
  (:gen-class)
  (:require [clj-terminal.terminal :as t]))


(defn basic-terminal
  []
  (let [tm (t/unix-terminal)]
    (t/set-fg-color tm :yellow)
    (t/put-string tm "this is basic terminal example\n")
    (t/set-fg-color tm :default)))                          ;restore default terminal colors