(ns oj.plots
  (:require [thi.ng.geom.viz.core :as viz]
            [thi.ng.geom.svg.core :as svg]
            [thi.ng.geom.core.vector :as v]
            [thi.ng.color.core :as col]
            [thi.ng.math.core :as m :refer [PI TWO_PI]]))

(def plot-width 1000)

(def plot-height 1000)

(defn export-viz
  [spec path]
  (->> spec
       (viz/svg-plot2d-cartesian)
       (svg/svg {:width plot-width :height plot-height})
       (svg/serialize)
       (spit path)))

(defn plot-fun-numbers [{:keys [anywhere big-end little-end start end plotfile]}]
  (println "Plotting start, end: " start " , " end)
  (println "Plotting data count anywhere, big, little: "
           (count anywhere)
           " , "
           (count big-end)
           " , "
           (count little-end))
  (println "Plotfile: " plotfile)
  (let [xs          (range start end)
        x-minor     (/ end 25)
        x-major     (/ end 5)
        anywheres   (flatten (map second anywhere))
        big-ends    (flatten (map second big-end))
        little-ends (flatten (map second little-end))
        ymin        (apply min (concat anywheres big-ends little-ends))
        ymax        (apply max (concat anywheres big-ends little-ends))
        y-minor     (/ ymax 25)
        y-major     (/ ymax 5)
        spec        {:x-axis (viz/linear-axis
                               {:domain     [start end]
                                :range      [50 (- plot-width 50)]
                                :pos        (- plot-width 50)
                                :major      x-major
                                :minor      x-minor
                                :label-dist 200})
                     :y-axis (viz/linear-axis
                               {:domain      [ymin ymax]
                                :range       [(- plot-height 50) 50]
                                :major       y-major
                                :minor       y-minor
                                :pos         50
                                :label-dist  200
                                :label-style {:text-anchor "end"}})
                     :grid   {:attribs {:stroke "#caa"}
                              :minor-x true
                              :minor-y true}
                     :data   [{:values  anywhere
                               :attribs {:fill "none" :stroke "#a0f"}
                               :shape   (viz/svg-triangle-down 3)
                               :layout  viz/svg-scatter-plot}

                              {:values  big-end
                               :attribs {:fill "none" :stroke "#bf0"}
                               :shape   (viz/svg-triangle-down 1)
                               :layout  viz/svg-scatter-plot}

                              {:values  little-end
                               :attribs {:fill "none" :stroke "#cff"}
                               :shape   (viz/svg-triangle-down 1)
                               :layout  viz/svg-scatter-plot}]}]
    (export-viz spec plotfile)))
