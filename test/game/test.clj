(ns game.test
  (:require [clojure.data [json :as json]])
  (:use clojure.test)
  (:use ai.core)
  (:use game.core))

(deftest indexToCartesian
  (is (=
       {:x 5 :y 3 :i 35}
       (pos 10 35))))

(deftest cartesianToIndex
  (is (=
       35
       (cell-index 10 5 3))))

(deftest peelSurface
  (is (=
       [{:i 0 :x 0 :y 0 :height 2 :substance :dirt}]
       (surface [[:floor :dirt]]))))

(deftest boundsProtected
  (is (= 0 (in-bounds 10 0)))
  (is (= 3 (in-bounds 10 3)))
  (is (= 9 (in-bounds 10 9)))
  (is (not (in-bounds 10 -1)))
  (is (not (in-bounds 10 10))))

(def small-world
  (vec (map (fn [i] (vector :floor))
            (range 16))))

(deftest small-world-works
  (is (=
       [
        [:floor] [:floor] [:floor] [:floor]
        [:floor] [:floor] [:floor] [:floor]
        [:floor] [:floor] [:floor] [:floor]
        [:floor] [:floor] [:floor] [:floor]
        ]
       small-world)))

(deftest side-count
  (is (=
       2
       (side [1 2 3 4]))))

(deftest edgeNodesDiscarded
  (let [network (surface-network small-world)
        counts (map count network)
        partitioned (vec(partition 4 counts))]
    (is (=
         (json/write-str [[3 5 5 3]
                          [5 8 8 5]
                          [5 8 8 5]
                          [3 5 5 3]])
         (json/write-str partitioned)))))

(def smallest-world
  (vec (map (fn [i] (vector :floor))
            (range 4))))

(deftest networkToGraph
  (let [network (surface-network smallest-world)
        expected {
                  0 {1 10
                     2 10
                     3 14}
                  1 {0 10
                     2 14
                     3 10}
                  2 {0 10
                     1 14
                     3 10}
                  3 {0 14
                     1 10
                     2 10}
                  }
        actual (connection-graph network)]
    (is (= expected actual))))

(deftest allPaths
  (let [net (surface-network small-world)
        graph (connection-graph net)
        children (fn [net node]
                   (keys (net node)))
        distance (fn[net nodesrc nodedest]
                   ((net nodesrc) nodedest))
        paths (vec(all-paths graph 0 children distance))]
    (is(= 16 (count paths)))
    (is(= '(0 0) (paths 0)))
    (is(= '(0 1 2 7) (paths 7)))
    (is(= '(0 5 10 15) (paths 15)))))

(deftest dijkstraOnRealData
  (let [net (surface-network small-world)
        graph (connection-graph net)
        children (fn [net node]
                   (keys (net node)))
        distance (fn[net nodesrc nodedest]
                   ((net nodesrc) nodedest))]
    (is(= ['(0 1 2 3) 30] (shortest-path graph 0 3 children distance)))
    (is(= ['(0 5 10 15) 42] (shortest-path graph 0 15 children distance)))
    (is(= ['(12 9 6 3) 42] (shortest-path graph 12 3 children distance)))))
