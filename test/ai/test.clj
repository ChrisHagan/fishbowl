(ns ai.test
  (:use clojure.test)
  (:use game.test)
  (:use game.core)
  (:use ai.core))

(deftest universeIsSane
  (is (= 5 (+ 2 3))))

(def net {:A {:B 85 :C 217 :E 173}
          :B {:F 80}
          :C {:G 186 :H 103}
          :D {}
          :E {:J 502}
          :F {:I 250}
          :G {}
          :H {:D 183 :J 167}
          :I {:J 84}
          :J {}
          })

(deftest dikstraWorks
  (let [pathinfo (shortest-path net :A :J children distance)]
    (is (= ['(:A :C :H :J) 487] pathinfo))))
