(ns examples.core
  (:gen-class)
  (:require [examples.terminal :as t]))

(def examples #{"basic-terminal"})

(defn print-help
  []
  (do
    (println "usage: examples.jar <arg>")
    (println "available args:")
    (doseq [x examples]
      (println "\t" x))
    (System/exit 0)))

(defn run-example
  [param]
  (case param
    "basic-terminal" (t/basic-terminal)
    (print-help)))

(defn -main
  "entry point."
  [& args]
  (let [param (first args)]
    (if (zero? (count args))
      (print-help)
      (run-example param))))
