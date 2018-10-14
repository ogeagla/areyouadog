(ns oj.clj-funumbers.viz
  (:require [thi.ng.geom.svg.core :as svg]
            [thi.ng.geom.viz.core :as viz]
            [thi.ng.color.gradients :as grad]
            [me.raynes.fs :as fs]
            [thi.ng.math.core :as m]))

(defn export-viz
  [spec path]
  (->> spec
       (viz/svg-plot2d-cartesian)
       (svg/svg {:width 1200 :height 800})
       (svg/serialize)
       (spit path)))

(defn spec
  [data x-dom y-dom]
  {:x-axis (viz/linear-axis
             {:domain x-dom
              :range  [50 1190]
              :pos    1190})
   :y-axis (viz/linear-axis
             {:domain      y-dom
              :range       [750 20]
              :pos         50})
   :grid   {:attribs {:stroke "#caa"}
            :minor-x true
            :minor-y true
            :fill    "white"}
   :data   [{:values  data
             :attribs {:fill "black" :stroke "none"}
             :layout  viz/svg-scatter-plot
             :shape   (viz/svg-triangle-down 2)}]})


(defn doit [data x-dom y-dom outfile]
  (-> (spec data x-dom y-dom)
      (export-viz outfile)))
