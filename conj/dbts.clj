(def card-types #{:hat :flag :pistol :sword :bottle :keys})

(defn draw
  [n] (repeatedly n #(rand-nth (vec card-types))))

(defn gen-board-sequence []
  (flatten [:start (repeatedly 6 #(shuffle card-types)) :boat]))

(defn create-turn-sequence [player-colors]
  (zipmap player-colors (rest (cycle player-colors))))
