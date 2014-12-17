(defproject pirates "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :plugins [[codox "0.6.4"][lein-marginalia "0.7.1"] [lein-kibit "0.0.8"]]
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
   :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 ;[org.clojure/data.json "0.2.3"]
                 [cljinterop/test "1.0-SNAPSHOT"]]
  :aot :all
  :main pirates.swingui
  )
