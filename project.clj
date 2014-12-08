(defproject magnet "0.1.0-SNAPSHOT"
  :description "Magnet loturak: zerbitzaria"
  :url "https://github.com/lnmnd/magnet.zer"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.8"]
                 [ring-cors "0.1.4"]
                 [clj-json "0.3.2"]
                 [org.clojure/java.jdbc "0.3.5"]
                 [com.h2database/h2 "1.3.170"]
                 [clj-time "0.8.0"]
                 [clj-bcrypt-wrapper "0.1.0"]
                 [http-kit "2.1.16"]
                 [org.clojure/data.codec "0.1.0"]]
  :plugins [[lein-midje "3.1.3"]
            [lein-ring "0.8.11"]
            [codox "0.8.10"]]
  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :main magnet.handler.main/-main
  :repl-options {:init-ns magnet.user}
  
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [org.clojure/tools.namespace "0.2.7"]
                        [midje "1.6.3"]]
         :ring {:handler magnet.handler/app}
         :codox {:src-dir-uri "http://github.com/lnmnd/magnet.zer/blob/master/"
                 :src-linenum-anchor-prefix "L"}}
   :uberjar {:aot :all}})
