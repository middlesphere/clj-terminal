(ns examples.core
  (:gen-class)
  (:require [examples.terminal :as t]))

(def examples #{"basic-terminal" "private-mode" "print-chars" "print-chars-pos cur-pos colors"
                "read-input"})

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
    "private-mode" (t/private-mode)
    "print-chars" (t/print-chars)
    "print-chars-pos" (t/print-chars-pos)
    "cur-pos" (t/cur-pos)
    "colors" (t/colors)
    "read-input" (t/read-input)
    (print-help)))

(defn -main
  "entry point."
  [& args]
  (let [param (first args)]
    (if (zero? (count args))
      (print-help)
      (run-example param))))
