(defproject pirates "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.48"]
                 [com.jme3/jme3-core "3.1.0-SNAPSHOT"]
                 [com.jme3/jme3-desktop "3.1.0-SNAPSHOT"]
                 [com.jme3/jme3-lwjgl "3.1.0-SNAPSHOT"]]

  :main pirates.swingui

  :jar-exclusions [#"\.swp|\.swo|\.DS_Store"]
  :profiles {:uberjar {:aot :all}
             :dev {:plugins [[lein-cljsbuild "1.1.0"]
                             [org.clojure/clojurescript "1.7.48"]]}
             :cljs {:plugins [[lein-cljsbuild "1.1.0"]] }}

  :source-paths ["src/clj" "src/cljc"]

  :clj {:builds [{ :source-paths ["src/clj" "src/cljc" "test"] }]}

  :cljsbuild {:builds [{ :source-paths ["src/cljs" "src/cljc"]
                        :compiler { :output-to "resources/public/js/pirates.js"
                                   :optimizations :advanced
                                   :pretty-print true}}]}

  :codox {:sources ["src/clj" "target/classes"]}

  :aot :all

  :repositories [["jme" "http://updates.jmonkeyengine.org/maven/"]])
