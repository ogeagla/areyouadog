(defproject clojureapp "0.1.0-SNAPSHOT"
    :plugins [[lein-ring "0.11.0"]]
    :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                   [http-kit "2.2.0"]]
    :local-repo ~(System/getenv "M2_REPO")
    :main webapp)
