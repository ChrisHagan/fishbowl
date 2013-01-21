(ns game.core
  (:use ai.core))

(def world (vec (map
                 (fn [i] (vector :floor))
                 (range (* 20 20)))))

(def population (atom {}))

(defn pos [width i]
  {:x (rem i width)
   :y (quot i width)
   :i i
   })

(defn cell-index [width x y]
  (+ x (* y width)))

(defn side [space]
  (Math/round(Math/sqrt (count space))))

(defn surface [space]
  (let [width (side space)]
    (vec (map-indexed
          (fn [i stack]
            (let [p (pos width i)]
              (assoc p :height (count stack) :substance (peek stack))))
          space))))

(defn in-bounds [limit i]
  (if (and
       (>= i 0)
       (< i limit))
    i
    nil))

(defn grid-neighbours [{x :x y :y height :height substance :substance} surf]
  (let [width (side surf)
        left (= x 0)
        right (= x (- width 1))
        top (= y 0)
        bottom (= y (- width 1))
        diagonal 14
        straight 10
        neighbours (atom '())
        link (fn [x y distance]
               (let [neighbour (assoc (surf(cell-index width x y)) :distance distance)]
                 (swap! neighbours conj neighbour)))]
    (if(not left)
      (let [nx (- x 1)]
        (link nx y straight)
        (if(not top)
          (link nx (- y 1) diagonal))
        (if(not bottom)
          (link nx (+ y 1) diagonal))))
    (if(not top)
      (link x (- y 1) straight))
    (if(not bottom)
      (link x (+ y 1) straight))
    (if(not right)
      (let [nx (+ x 1)]
        (link nx y straight)
        (if(not top)
          (link nx (- y 1) diagonal))
        (if(not bottom)
          (link nx (+ y 1) diagonal))))
    @neighbours))

(defn surface-network [space]
  (let [surf (surface space)]
    (map
     (fn [surface-cell] (grid-neighbours surface-cell surf))
     surf)))

(defn connection-graph [network]
  (let [graph (atom {})
        link-entry (fn[link] [(:i link) (:distance link)])
        connect (fn [i links]
                  (swap! graph assoc i (into {} (map link-entry links))))]
    (doall(map-indexed connect network))
    @graph))

(defn initialize-user [id]
  {:id id
   :x 0
   :y 0
   :mode :collecting
   })

(defn children [net node]
  (keys (net node)))

(defn distance [net nodesrc nodedest]
  ((net nodesrc) nodedest))

(defn tick []
  (let [net (connection-graph(surface-network world))]
    (doseq [user @population]
      (let [cell (cell-index (:x user) (:y user))
            target (rand-int (count world))]
        (printf "Ticking user %s\n" user)
        ))))

(defn birth-user [id]
  (swap! population assoc id (initialize-user id)))
