(defproject clojureapp "0.1.0-SNAPSHOT"
  :plugins [[lein-ring "0.11.0"]]
  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [http-kit "2.2.0"]
                 [compojure "1.5.2"]
                 [thi.ng/geom "0.0.908"]]
  ;:local-repo ~(System/getenv "M2_REPO")
  :main oj.webapp
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}}
  :ring {:handler oj.webapp/ze-routes})
