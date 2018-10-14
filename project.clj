(defproject clojureapp "0.1.0-SNAPSHOT"
  :plugins [[lein-ring "0.11.0"]
            [lein-ancient "0.6.14"]
            [jonase/eastwood "0.2.5"]
            [lein-kibit "0.1.5"]
            [lein-bikeshed "0.5.0"]]
  :dependencies [[org.clojure/clojure "1.10.0-beta3"]
                 [instaparse "1.4.9"]
                 [http-kit "2.3.0"]
                 [compojure "1.6.1"]
                 [thi.ng/geom "0.0.1062"]
                 [hiccup "1.0.5"]
                 [clj-fuzzy "0.4.1"]
                 [clojure-opennlp "0.5.0"]
                 [me.raynes/fs "1.4.6"]
                 [com.taoensso/nippy "2.14.0"]]
  :local-repo ~(System/getenv "M2_REPO")
  :main oj.webapp
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}}
  :ring {:handler oj.webapp/ze-routes})
