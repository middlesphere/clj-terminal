(ns clj-terminal.screen-spec
  (:gen-class)
  (:require [clojure.spec :as s]
            [clojure.spec.test :as test]
            [clojure.spec.gen :as gen])
  (:import (com.googlecode.lanterna.screen Screen)
           (com.googlecode.lanterna.terminal Terminal)))

;(s/check-asserts true)

(alias 'sc 'clj-terminal.screen)

(s/def ::sc/screen-instance #(instance? Screen %))

(s/fdef sc/new-screen
        :args (s/or :args0 #(empty? %)
                    :args1 (s/cat :tm #(instance? Terminal %)))
        :ret ::sc/screen-instance)

(s/fdef sc/start
        :args (s/cat :ts ::sc/screen-instance)
        :ret nil?)

(s/fdef sc/stop
        :args (s/cat :ts ::sc/screen-instance)
        :ret nil?)

(s/def ::sc/refresh-types #{:delta :auto :full})

(s/fdef sc/refresh
        :args (s/or :arg1 (s/cat :ts ::sc/screen-instance)
                    :arg2 (s/cat :ts ::sc/screen-instance :type ::sc/refresh-types))
        :ret nil?)

(s/fdef sc/get-terminal
        :args (s/cat :ts ::sc/screen-instance)
        :ret #(instance? Terminal %))

(s/def ::sc/coords (s/and (s/keys :req-un [::col ::row])
                          (s/map-of keyword? nat-int?)))

(s/fdef sc/resize-if-necessary
        :args (s/cat :ts ::sc/screen-instance)
        :ret (s/or :new-size ::sc/coords
                   :not-changed nil?))

(s/fdef sc/scroll-lines
        :args (s/cat :ts ::sc/screen-instance :first-line nat-int? :last-line nat-int? :distance int?)
        :ret nil?)

(s/def ::sc/text-colors #{:black :white :red :green :blue :cyan :magenta :yellow :default})
(s/def ::sc/text-effects #{:blink :bold :bordered :circled :crossed :fraktur :reverse :underline})

(s/def ::fg ::sc/text-colors)
(s/def ::bg ::sc/text-colors)
(s/def ::eff (s/coll-of ::sc/text-effects))

(s/def ::sc/color-params (s/keys :req-un [::fg ::bg] :opt-un [::eff]))

(s/fdef sc/rectangle
        :args (s/cat :ts ::sc/screen-instance :col nat-int? :row nat-int?
                     :width nat-int? :height nat-int? :c char? :colors (s/? ::sc/color-params))
        :ret any?)

(s/fdef sc/fill-rectangle
        :args (s/cat :ts ::sc/screen-instance :col nat-int? :row nat-int?
                     :width nat-int? :height nat-int? :c char? :colors (s/? ::sc/color-params))
        :ret any?)


(s/fdef sc/put-character
        :args (s/cat :ts ::sc/screen-instance :c char? :col nat-int? :row nat-int? :colors (s/? ::sc/color-params))
        :ret any?)

(s/fdef sc/put-string
        :args (s/cat :ts ::sc/screen-instance :s string? :col nat-int? :row nat-int? :colors (s/? ::sc/color-params))
        :ret any?)

(s/fdef sc/line
        :args (s/cat :ts ::sc/screen-instance :from-col nat-int? :from-row nat-int?
                     :to-col nat-int? :to-row nat-int? :c char? :colors (s/? ::sc/color-params))
        :ret any?)