(ns pirates.actions
  (:require
    [pirates.pieces :as pieces])
  (:gen-class
    :methods [#^{:static true} [draw [int] Object]]
    ))

(defn draw
  "Draw n cards. Note that rather than having a deck we assume an
  infinite number of uniformly distributed cards."
  [n] (repeatedly n #(rand-nth (vec pieces/card-types))))

(defn cards-to
  ([player card](update-in player [card] #(inc %)))
  ([player card & more](reduce #(cards-to %1 %2) (cards-to player card) more)))

;;Generated functions
(defn -draw [n] (draw n))

(defn space-available? [piece-slots index]
  (and (< index (count piece-slots)) (< 0 (count (get piece-slots index)) 3)))
(defn space-occupied? [piece-slots index] (not= (count (get piece-slots index)) 0))

(defn pirate-on-space? [pirate piece-slots index] (contains? (get piece-slots index) pirate))
(defn find-pirate [pirate piece-slots] (first (filter #(contains? (get piece-slots %) pirate) (range (count piece-slots)))))
(defn symbol-indices
  ([symbol board start] (filter #(= symbol (get board %)) (range start (count board))))
  ([symbol board] (symbol-indices symbol board 0)))

(defn next-open [start-index symbol board piece-slots]
  (or
    (first
      (filter #(not (space-occupied? piece-slots %)) (symbol-indices symbol board start-index)))
    (dec (count board))))

(defn next-fallback [start-index piece-slots]
  (first (filter #(space-available? piece-slots %) (range (dec start-index) 0 -1))))

(defn play-card [card pirate board piece-slots]
  (let [from-index (find-pirate pirate piece-slots)
        to-index (next-open (inc from-index) card board piece-slots)
        removed (disj (get piece-slots from-index) pirate)
        added (conj (get piece-slots to-index) pirate)]
    (assoc (assoc piece-slots from-index removed) to-index added)))

(defn execute-fall-back [from-index to-index pirate piece-slots players]
  (let [removed (disj (get piece-slots from-index) pirate)
        dest (get piece-slots to-index)
        added (conj dest pirate)
        num-cards (count dest)
        player (first (filter #(= (:color pirate) (:color %)) players))
        newplayers (filter #(not= % player) players)
        newplayer (update-in player [:cards] #(apply cards-to % (draw num-cards)))]
    [(assoc (assoc piece-slots from-index removed) to-index added)
     (conj newplayers newplayer)]))

(defn fall-back [pirate piece-slots players]
  (let [from-index (find-pirate pirate piece-slots)
        to-index (or (next-fallback from-index piece-slots) from-index)]
    (if (= from-index to-index)
      [piece-slots players]
      (execute-fall-back from-index to-index pirate piece-slots players))))

(defn winner? [pieces color] (= 6 (count (filter #(= (:color %) color) (last pieces)))))

;(defn take-turn [initial-player initial-pieces-slots done?]
;  (loop [player initial-player pieces-slots initial-pieces-slots turn 0]
;    (if (or (= 3 turn) (done?))
;      2
;      (recur 345 123 (inc turn)))))
