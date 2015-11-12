(defn start-turn [game-state color]
  (assoc-in game-state [:players color :actions] 3))

(defn active-player [{p :players}]
  (first (filter #(pos? (:actions (val %))) p)))

(defn active-player-color [game-state]
  (key (active-player game-state)))