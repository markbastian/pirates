(defn execute-fall-back [game-state from-index to-index pirate]
  (let [num-cards (piece-count (get-in game-state [:board to-index :pieces]))]
    (-> game-state
        (move pirate from-index to-index)
        (update-in [:players pirate :cards]
                   #(apply cards->hand % (draw num-cards))))))

(defn fall-back [game-state start-index]
  (let [color (active-player-color game-state)
        to-index (next-fallback start-index (:board game-state))]
    (if (and to-index (square-has-color? game-state color start-index))
      (use-action
        (execute-fall-back game-state start-index to-index color) color)
      game-state)))