(ns oj.webapp
  (:use org.httpkit.server)
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :refer [site]]
            [oj.nvc.core :as nvc]
            [oj.nvc.dictionary :as nvcd]
            [hiccup.core :refer :all]
            [hiccup.form :refer :all]))

(defonce server (atom nil))

(defn stop-server []
  (when-not (nil? @server)
    ;; graceful shutdown: wait 100ms for existing requests to be finished
    ;; :timeout is optional, when no timeout, stop immediately
    (@server :timeout 100)
    (reset! server nil)))

(defn map-tag [tag xs]
  (map (fn [x] [tag x]) xs))

(def homepage
  (html
    [:h2 "Causal words: "]
    [:ul
     (for [cw nvcd/all-causal-words]
       [:li cw])]
    [:h2 "Primary feelings: "]
    [:ul
     (for [cw nvcd/all-primary-feelings]
       [:li cw])]
    [:h2 "Underlying needs: "]
    [:ul
     (for [cw nvcd/all-underlying-needs]
       [:li cw])]

    [:h2 "Needs are being met?: "]
    [:table
     [:tr (map-tag :th ["are met" "not met"])]
     [:tr (for [{:keys [needs-not-met needs-are-met]} nvcd/feeling-words]
            [:tr [:td (str needs-are-met)] [:td (str needs-not-met)]])]]))

(defroutes ze-routes
           (GET "/" [] homepage))

(defn -main [& args]
  (let [port (Integer/parseInt (get (System/getenv) "OPENSHIFT_CLOJURE_HTTP_PORT" "8080"))
        ip   (get (System/getenv) "OPENSHIFT_CLOJURE_HTTP_IP" "0.0.0.0")]
    (println "Running app on: " ip ":" port)
    ;; The #' is useful when you want to hot-reload code
    ;; You may want to take a look: https://github.com/clojure/tools.namespace
    ;; and http://http-kit.org/migration.html#reload
    (reset! server (run-server
                     (site #'ze-routes)
                     {:port port :ip ip}))))
