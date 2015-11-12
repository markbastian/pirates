(defn winner? [{ :keys [board] :as game-state }]
  (let [color (active-player-color game-state)]
    (-> board last :pieces color (= 6))))

(defn space-available? [board index]
  (and (< index (count board))
       (< 0 (or (piece-count (:pieces (board index))) 0) 3)))

(defn space-occupied? [game-state index]
  (-> game-state (get-in [:board index :pieces]) piece-count pos?))

(defn player-has-card? [game-state card color]
  (pos? (get-in game-state [:players color :cards card])))

(defn square-has-color? [game-state color index]
  (pos? (get-in game-state [:board index :pieces color])))

(defn players-turn? [game-state color]
  (pos? (get-in game-state [:players color :actions])))