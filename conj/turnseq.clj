(defn create-turn-sequence [player-colors]
  (zipmap player-colors (rest (cycle player-colors))))
