(defproject magnet "1.0.0"
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
                 [javax.servlet/servlet-api "2.5"]
                 [http-kit "2.1.16"]
                 [org.clojure/data.codec "0.1.0"]]
  :plugins [[lein-midje "3.1.3"]
            [codox "0.8.10"]]
  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :main magnet.core
  
  :profiles
  {:dev {:dependencies [[org.clojure/tools.namespace "0.2.7"]
                        [midje "1.6.3"]]
         :codox {:src-dir-uri "http://github.com/lnmnd/magnet.zer/blob/master/"
                 :src-linenum-anchor-prefix "L"
                 :project {:name "Magnet", :version "1.0.0", :description "Magnet loturak: zerbitzaria"}}}
   :uberjar {:aot :all}})
