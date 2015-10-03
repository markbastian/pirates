(ns pirates.rules)

;The potential cards you can get.
(def card-types #{:hat :flag :pistol :sword :bottle :keys})

(def empty-hand (zipmap card-types (repeat 0)))

;Pawn types - id'd by color.
(def pirate-colors #{:red :green :blue :yellow :brown})

(defn draw
  "Draw n cards. Note that rather than having a deck we assume an
  infinite number of uniformly distributed cards."
  [n] (repeatedly n #(rand-nth (vec card-types))))

(defn gen-board [] (into (reduce into [:start] (repeat 6 (shuffle card-types))) [:boat]))

(defn cards->hand
  ([hand card](update-in hand [card] inc))
  ([hand card & more](reduce #(cards->hand %1 %2) (cards->hand hand card) more)))

(defn init-pieces [used-colors board]
  (let [empty-color-map (zipmap used-colors (repeat 0))
        empty-slots (vec (repeat (count board) empty-color-map))]
    (update-in empty-slots [0] #(zipmap (keys %) (repeat 6)))))

(defn make-board [used-colors]
  (let [board-sequence (gen-board)]
    { :symbols board-sequence
      :pieces (init-pieces used-colors board-sequence)}))

(defn initial-hand [] (apply cards->hand empty-hand (draw 6)))

(defn create-turn-sequence [player-colors] (zipmap player-colors (rest (cycle player-colors))))

(defn init-players [players]
  (zipmap
    (map :color players)
    (map #(into {} { :name (:name %) :cards (initial-hand) :actions 0 }) players)))

(defn active-player [{p :players}] (first (filter #(> (:actions (val %)) 0) p)))

(defn active-player-color [game-state](key (active-player game-state)))

(defn init-game-state [player-prefs]
  { :turn-order (create-turn-sequence (map :color player-prefs))
    :players (init-players player-prefs)
    :board (make-board (map :color player-prefs)) })

(defn winner? [{ { p :pieces } :board :as game-state }]
  (let [color (active-player-color game-state)]
    (-> p last color (= 6))))

(defn piece-count
  ([piece-slot] (reduce + (vals piece-slot)))
  ([piece-slots index] (piece-count (get piece-slots index))))

(defn space-available?
  ([piece-slots index] (and (< index (count piece-slots)) (space-available? (get piece-slots index))))
  ([piece-slot](< 0 (or (piece-count piece-slot) 0) 3)))

(defn space-occupied? [game-state index]
  (-> game-state (get-in [:board :pieces index]) piece-count pos?))

(defn symbol-indices
  ([symbol board start] (filter #(= symbol (get board %)) (range start (count board))))
  ([symbol board] (symbol-indices symbol board 0)))

(defn next-open [{ { :keys [pieces symbols] } :board :as game-state } start-index symbol]
  (let [n (count pieces) r (range start-index n)]
    (or
      (first (filter #(and (not (space-occupied? game-state %)) (= symbol (get symbols %))) r))
      (dec n))))

(defn next-fallback [start-index piece-slots]
  (first (filter #(space-available? piece-slots %) (range (dec start-index) 0 -1))))

(defn player-has-card? [game-state card color]
  (< 0 (or (get-in game-state [:players color :cards card]) 0)))

(defn square-has-color? [game-state color index]
  (< 0 (or (get-in game-state [:board :pieces index color]) 0)))

(defn players-turn? [game-state color]
  (pos? (get-in game-state [:players color :actions])))

(defn start-turn [game-state color]
  (assoc-in game-state [:players color :actions] 3))

(defn use-action [game-state color]
  (-> game-state
      (update-in [:players color :actions] dec)
      (#(if (zero? (get-in % [:players color :actions]))
         (start-turn % (get-in % [:turn-order color])) %))))

(defn pass [game-state]
  (let [color (active-player-color game-state)]
    (if (players-turn? game-state color)
      (start-turn
        (assoc-in game-state [:players color :actions] 0)
        (get-in game-state [:turn-order color]))
      game-state)))

(defn space-contents
  "Convert the pieces on the given space to a vector of pieces"
  ([space] (vec (reduce concat (map #(repeat (val %) (key %)) space))))
  ([game-state index]
   (space-contents (get-in game-state [:board :pieces index]))))

(defn hand-contents
  "Convert the player's cards (map of cards to count) to a vector of cards."
  ([hand] (vec (reduce concat (map #(repeat (val %) (key %)) hand))))
  ([game-state color]
   (hand-contents (get-in game-state [:players color :cards]))))

(defn execute-play-card [game-state card-symbol pirate-color from-index]
  (let [to-index (next-open game-state (inc from-index) card-symbol)]
    (-> game-state
        (update-in [:board :pieces from-index pirate-color] dec)
        (update-in [:board :pieces to-index pirate-color] inc)
        (update-in [:players pirate-color :cards card-symbol] dec))))

(defn play-card [game-state card from-index]
  (let [color (active-player-color game-state)]
    (if (and (player-has-card? game-state card color)
           (square-has-color? game-state color from-index)
           (players-turn? game-state color))
    (use-action (execute-play-card game-state card color from-index) color)
    game-state)))

(defn execute-fall-back [game-state from-index to-index pirate]
  (let [num-cards (piece-count (get-in game-state [:board :pieces to-index]))]
    (-> game-state
        (update-in [:board :pieces from-index pirate] dec)
        (update-in [:board :pieces to-index pirate] inc)
        (update-in [:players pirate :cards] #(apply cards->hand % (draw num-cards))))))

(defn fall-back [game-state start-index]
  (let [color (active-player-color game-state)
        to-index (next-fallback start-index (get-in game-state [:board :pieces]))]
    (if (and to-index
             (square-has-color? game-state color start-index)
             (players-turn? game-state color))
      (use-action (execute-fall-back game-state start-index to-index color) color)
      game-state)))
