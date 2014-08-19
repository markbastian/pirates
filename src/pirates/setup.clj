(ns pirates.setup
  (:require [pirates.pieces :as pieces]
            [pirates.actions :as actions]))

(defn gen-board [] (conj (vec (conj (apply concat (repeat 6 (shuffle pieces/card-types))) :start)) :boat))

(defn init-pieces [used-colors board]
  (let [used-pieces (apply concat (map #(% pieces/pieces) used-colors))
        empty-slots (vec (repeat (count board) #{}))]
     (update-in empty-slots [0] #(into % used-pieces))))

(def empty-hand (zipmap pieces/card-types (repeat 0)))

(defn initial-hand [] (apply actions/cards-to empty-hand (actions/draw 6)))

(defn init-players [players] (map #(into % {:cards (initial-hand)}) players))
