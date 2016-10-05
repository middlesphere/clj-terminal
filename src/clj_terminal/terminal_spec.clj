(ns clj-terminal.terminal-spec
  (:gen-class)
  (:require [clojure.spec :as s]
            [clojure.spec.test :as test]
            [clojure.spec.gen :as gen])
  (:import (com.googlecode.lanterna.terminal.ansi UnixTerminal)))

;(s/check-asserts true)

(alias 't 'clj-terminal.terminal)

(s/def ::t/unix-instance #(instance? UnixTerminal %))

(s/def ::t/colors #{:black :white :red :green :blue :cyan :magenta :yellow :default})

#_(s/fdef t/default-terminal
        :args empty?
        :ret ::t/unix-instance)

(s/fdef t/unix-terminal
        :args empty?
        :ret ::t/unix-instance)

(s/fdef t/enter-private-mode
        :args (s/cat :t ::t/unix-instance)
        :ret nil?)

(s/fdef t/exit-private-mode
        :args (s/cat :t ::t/unix-instance)
        :ret nil?)

(s/def ::t/coords (s/and (s/keys :req-un [::col ::row])
                         (s/map-of keyword? nat-int?)))

(s/fdef t/get-cursor-position
        :args (s/cat :t ::t/unix-instance)
        :ret ::t/coords)

(s/fdef t/set-cursor-position
        :args (s/or :args2 (s/cat :t ::t/unix-instance :coord ::t/coords)
                    :args3 (s/cat :t ::t/unix-instance :col nat-int? :row nat-int?))
        :ret nil?)

(s/fdef t/clear
        :args (s/cat :t ::t/unix-instance)
        :ret nil?)

(s/fdef t/set-bg-color
        :args (s/cat :t ::t/unix-instance :c ::t/colors)
        :ret nil?)

(s/fdef t/set-fg-color
        :args (s/cat :t ::t/unix-instance :c ::t/colors)
        :ret nil?)

(s/fdef t/get-terminal-size
        :args (s/cat :t ::t/unix-instance)
        :ret ::t/coords)

(s/fdef t/put-character
        :args (s/or :args2 (s/cat :t ::t/unix-instance :c char?)
                    :args4 (s/cat :t ::t/unix-instance :c char? :col nat-int? :row nat-int?))
        :ret nil?)

(s/fdef t/put-string
        :args (s/or :args2 (s/cat :t ::t/unix-instance :s string?)
                    :args4 (s/cat :t ::t/unix-instance :s string? :col nat-int? :row nat-int?))
        :ret nil?)

(s/fdef t/visible-cursor
        :args (s/cat :t ::t/unix-instance :b boolean?)
        :ret nil?)

(s/def ::t/decoded-input (s/keys :req-un [::value ::type]
                                 :opt-un [::ctrl ::alt ::shift]))

(s/fdef t/read-input
        :args (s/cat :t ::t/unix-instance)
        :ret ::t/decoded-input)

(s/fdef t/poll-input
        :args (s/cat :t ::t/unix-instance)
        :ret ::t/decoded-input)

(s/fdef t/bell
        :args (s/cat :t ::t/unix-instance)
        :ret nil?)

(s/fdef t/maximize
        :args (s/cat :t ::t/unix-instance)
        :ret nil?)

(s/fdef t/unmaximize
        :args (s/cat :t ::t/unix-instance)
        :ret nil?)

(s/fdef t/new-size
        :args (s/cat :t ::t/unix-instance :col nat-int? :row nat-int?)
        :ret nil?)

(s/def ::t/effects #{:blink :bold :bordered :circled :crossed :fraktur :reverse :underline})

(s/fdef t/text-effect
        :args (s/cat :t ::t/unix-instance :eff ::t/effects :enable? boolean?)
        :ret nil?)

(s/fdef t/reset-color-eff
        :args (s/cat :t ::t/unix-instance)
        :ret nil?)


