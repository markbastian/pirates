(defn execute-play-card [game-state card-symbol pirate-color from-index]
  (let [to-index (next-open game-state (inc from-index) card-symbol)]
    (-> game-state
        (move pirate-color from-index to-index)
        (update-in [:players pirate-color :cards card-symbol] dec))))

(defn play-card [game-state card from-index]
  (let [color (active-player-color game-state)]
    (if (and (player-has-card? game-state card color)
             (square-has-color? game-state color from-index))
      (use-action (execute-play-card game-state card color from-index) color)
      game-state)))