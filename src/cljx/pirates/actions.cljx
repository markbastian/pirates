(ns pirates.actions)

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

(defn init-game-state [player-prefs]
  { :turn-order (create-turn-sequence (map :color player-prefs))
    :players (init-players player-prefs)
    :board (make-board (map :color player-prefs)) })

(defn winner? [{ { p :pieces } :board } color] (-> p last color (= 6)))

(defn piece-count
  ([piece-slot] (reduce + (vals piece-slot)))
  ([piece-slots index] (piece-count (get piece-slots index))))

(defn space-available?
  ([piece-slots index] (and (< index (count piece-slots)) (space-available? (get piece-slots index))))
  ([piece-slot](< 0 (or (piece-count piece-slot) 0) 3)))

(defn space-occupied? [index { { { i index } :pieces } :board }] (not= (piece-count i) 0))

(defn symbol-indices
  ([symbol board start] (filter #(= symbol (get board %)) (range start (count board))))
  ([symbol board] (symbol-indices symbol board 0)))

(defn next-open [start-index symbol { { :keys [pieces symbols] } :board :as game-state }]
  (let [n (count pieces) r (range start-index n)]
    (or
      (first (filter #(and (not (space-occupied? % game-state)) (= symbol (get symbols %))) r))
      (dec n))))

(defn next-fallback [start-index piece-slots]
  (first (filter #(space-available? piece-slots %) (range (dec start-index) 0 -1))))

(defn player-has-card? [card color game-state](< 0 (or (get-in game-state [:players color :cards card]) 0)))

(defn square-has-color? [color index game-state](< 0 (or (get-in game-state [:board :pieces index color]) 0)))

(defn players-turn? [color { { { actions :actions } color } :players }] (> (or actions 0) 0))

(defn start-turn [color game-state] (assoc-in game-state [:players color :actions] 3))

(defn use-action [color { { { actions :actions } color } :players { player-color color } :turn-order :as game-state }]
  (let [decreased (update-in game-state [:players color :actions] dec)]
    (if (= 1 actions) (start-turn player-color decreased) decreased)))

(defn pass [color game-state]
  (if (players-turn? color game-state)
    (start-turn
      (get-in game-state [:turn-order color])
      (assoc-in game-state [:players color :actions] 0))))

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

(defn execute-play-card [card-symbol pirate-color from-index game-state]
  (let [to-index (next-open (inc from-index) card-symbol game-state)
        removed (update-in game-state [:board :pieces from-index pirate-color] dec)
        added (update-in removed [:board :pieces to-index pirate-color] inc)
        de-carded (update-in added [:players pirate-color :cards card-symbol] dec)]
    de-carded))

;Should inline and use destructuring
(defn play-card [card color from-index game-state]
  (if (and (player-has-card? card color game-state)
           (square-has-color? color from-index game-state)
           (players-turn? color game-state))
    (use-action color (execute-play-card card color from-index game-state))
    game-state))

(defn execute-fall-back [from-index to-index pirate game-state]
  (let [removed (update-in game-state [:board :pieces from-index pirate] dec)
        num-cards (piece-count (get-in game-state [:board :pieces to-index]))
        added (update-in removed [:board :pieces to-index pirate] inc)]
    (update-in added [:players pirate :cards] #(apply cards->hand % (draw num-cards)))))

(defn fall-back [color start-index game-state]
  (let [to-index (next-fallback start-index (get-in game-state [:board :pieces]))]
    (if (and (not (nil? to-index))
             (square-has-color? color start-index game-state)
             (players-turn? color game-state))
      (use-action color (execute-fall-back start-index to-index color game-state))
      game-state)))

(defn active-player [{p :players}] (first (filter #(> (:actions (val %)) 0) p)))
