(defn init-game-state [player-prefs]
  { :turn-order (create-turn-sequence (map :color player-prefs))
   :players (init-players player-prefs)
   :board (setup-board (map :color player-prefs)) })