(defproject pirates "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2657"]]
  :auto-clean false
  :prep-tasks [["cljx" "once"]]
  :source-paths ["src/clj"]
  :profiles {:dev {:plugins [[org.clojure/clojurescript "0.0-2657"]
                             [com.keminglabs/cljx "0.5.0"]
                             [lein-cljsbuild "1.0.4"]
                             [codox "0.8.10"]
                             ;[lein-autodoc "0.9.0"]
                             ]
                   :aliases {"cleantest" ["do" "clean," "cljx" "once," "test," "cljsbuild" "test"]}}
             :cljs {:plugins [[lein-cljsbuild "1.0.4"]]}}

  :clj {:builds [{:source-paths ["src/clj" "target/classes" ;"target/test-classes"
                                 ]}]}

  :cljsbuild {:builds [{:source-paths ["src/cljs" "target/classes" ;"target/test-classes"
                                       ]
                        :compiler {:output-to "resources/public/js/pirates.js"
                                   :optimizations :advanced
                                   :pretty-print true}}]}

  :cljx {:builds [{:source-paths ["src/cljx"]
                   :output-path "target/classes"
                   :rules :clj}

                  {:source-paths ["src/cljx"]
                   :output-path "target/classes"
                   :rules :cljs}

                  {:source-paths ["test/cljx"]
                   :output-path "target/test-classes"
                   :rules :clj}

                  {:source-paths ["test/cljx"]
                   :output-path "target/test-classes"
                   :rules :cljs}]}

  :codox {:sources ["src/clj" "target/classes"]}

  :main pirates.swingui
  :aot :all)
