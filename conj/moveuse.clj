(defn move [game-state color from-index to-index]
  (-> game-state
      (update-in [:board from-index :pieces color] dec)
      (update-in [:board to-index :pieces color] inc)))

(defn use-action [game-state color]
  (-> game-state
      (update-in [:players color :actions] dec)
      (#(if (zero? (get-in % [:players color :actions]))
         (start-turn % (get-in % [:turn-order color])) %))))