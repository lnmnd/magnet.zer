(defproject magnet "0.1.0-SNAPSHOT"
  :description "Magnet loturak: zerbitzaria"
  :url "https://github.com/lnmnd/magnet.zer"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ring/ring-jetty-adapter "1.3.1"]
                 [compojure "1.1.8"]
                 [ring-cors "0.1.4"]
                 [clj-json "0.3.2"]
                 [org.clojure/java.jdbc "0.2.3"]
                 [com.h2database/h2 "1.3.170"]]
  :plugins [[lein-ring "0.8.11"]]
  :aot :all
  :ring {:handler magnet.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
