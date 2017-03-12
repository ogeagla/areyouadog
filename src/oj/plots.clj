(ns oj.plots
  (:require [thi.ng.geom.viz.core :as viz]
            [thi.ng.geom.svg.core :as svg]
            [thi.ng.geom.core.vector :as v]
            [thi.ng.color.core :as col]
            [thi.ng.math.core :as m :refer [PI TWO_PI]]))



(defn export-viz
  [spec path]
  (->> spec
       (viz/svg-plot2d-cartesian)
       (svg/svg {:width 1000 :height 1000})
       (svg/serialize)
       (spit path)))

(defn do-plot [numbers svg-output-filename]
  (let [xs   (flatten (map first numbers))
        xmin (apply min xs)
        xmax (apply max xs)
        ys   (flatten (map second numbers))
        ymin (apply min ys)
        ymax (apply max ys)
        spec {:x-axis (viz/linear-axis
                        {:domain [0 xmax]
                         :range  [50 590]
                         :pos    550})
              :y-axis (viz/linear-axis
                        {:domain      [0 ymax]
                         :range       [550 20]
                         :major       10
                         :minor       5
                         :pos         50
                         :label-dist  15
                         :label-style {:text-anchor "end"}})
              :grid   {:attribs {:stroke "#caa"}
                       :minor-x true
                       :minor-y true}
              :data   [
                       ;{:values  numbers
                       ; :attribs {:fill "#0af" :stroke "none"}
                       ; :layout  viz/svg-scatter-plot}
                       {:values  numbers
                        :attribs {:fill "none" :stroke "#f60"}
                        :shape   (viz/svg-triangle-down 8)
                        :layout  viz/svg-scatter-plot}
                       ]}]
    (export-viz spec svg-output-filename)))
