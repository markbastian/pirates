(ns pirates.setup
  (:require [pirates.pieces :as pieces]
            [pirates.actions :as actions]))

(defn gen-board [] (conj (vec (conj (apply concat (repeat 6 (shuffle pieces/card-types))) :start)) :boat))

(defn init-pieces [used-colors board]
  (let [empty-color-map (zipmap used-colors (repeat 0))
        empty-slots (vec (repeat (count board) empty-color-map))]
    (update-in empty-slots [0] #(zipmap (keys %) (repeat 6)))))

(defn make-board [used-colors]
  (let [board-sequence (gen-board)]
    { :symbols board-sequence
      :pieces (init-pieces used-colors board-sequence)}))

(def empty-hand (zipmap pieces/card-types (repeat 0)))

(defn initial-hand [] (apply actions/cards-to empty-hand (actions/draw 6)))

(defn init-players [players]
  (zipmap
    (map :color players)
    (map #(into {} { :name (:name %) :cards (initial-hand) :actions 0 }) players)))
