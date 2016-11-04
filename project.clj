(defproject com.middlesphere/clj-terminal "0.1.0-alpha"
  :description "Wrapper for Java Lanterna lib"
  :url "https://github.com/middlesphere/clj-terminal.git"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [com.googlecode.lanterna/lanterna "3.0.0-beta3"]]

  ;:main ^:skip-aot clj-terminal.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
