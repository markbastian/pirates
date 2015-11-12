(defn pass [game-state]
  (let [color (active-player-color game-state)]
    (start-turn
      (assoc-in game-state [:players color :actions] 0)
      (get-in game-state [:turn-order color]))))