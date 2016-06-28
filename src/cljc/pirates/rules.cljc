(ns pirates.rules)

;The potential cards you can get.
(def card-types #{:hat :flag :pistol :sword :bottle :keys})

;Pawn types - id'd by color.
(def pirate-colors #{:red :green :blue :yellow :brown})

(defn draw
  "Draw n cards. Note that rather than having a deck we assume an
  infinite number of uniformly distributed cards."
  [n] (repeatedly n #(rand-nth (vec card-types))))

(defn gen-board-sequence []
  (flatten [:start (repeatedly 6 #(shuffle card-types)) :boat]))

(defn add-initial-pieces [board colors]
  (assoc-in board [0 :pieces] (zipmap colors (repeat 6))))

(defn make-board [used-colors]
  (let [empty-color-map { :pieces (zipmap used-colors (repeat 0)) }]
    (mapv #(assoc empty-color-map :symbol %) (gen-board-sequence))))

(defn setup-board [colors]
  (add-initial-pieces (make-board colors) colors))

(defn cards->hand
  ([hand card](update-in hand [card] inc))
  ([hand card & more](reduce #(cards->hand %1 %2)
                             (cards->hand hand card) more)))

(def empty-hand (zipmap card-types (repeat 0)))

(defn initial-hand [] (apply cards->hand empty-hand (draw 6)))

(defn init-players [players]
  (zipmap
    (map :color players)
    (map (fn [p]{ :name (:name p) :cards (initial-hand) :actions 0 })
         players)))

(defn active-player [{p :players}]
  (first (filter #(pos? (:actions (val %))) p)))

(defn active-player-color [game-state](key (active-player game-state)))

(defn create-turn-sequence [player-colors] (zipmap player-colors (rest (cycle player-colors))))

(defn init-game-state [player-prefs]
  { :turn-order (create-turn-sequence (map :color player-prefs))
    :players (init-players player-prefs)
    :board (setup-board (map :color player-prefs)) })

(defn winner? [{ :keys [board] :as game-state }]
  (let [color (active-player-color game-state)]
    (-> board last :pieces color (= 6))))

(defn piece-count[piece-slot]
  (reduce + (vals piece-slot)))

(defn fallback-space-available? [board index]
  (and (< index (count board))
       (< 0 (or (piece-count (:pieces (board index))) 0) 3)))

(defn space-occupied? [game-state index]
  (-> game-state (get-in [:board index :pieces]) piece-count pos?))

(defn symbol-indices
  ([symbol board start] (filter #(= symbol (get board %)) (range start (count board))))
  ([symbol board] (symbol-indices symbol board 0)))

(defn next-open [{ :keys [board] :as game-state } start-index symbol]
  (let [r (range start-index (count board))]
    (or
      (first (filter #(and (not (space-occupied? game-state %))
                           (= symbol (get-in board [% :symbol]))) r))
      (dec (count board)))))

(defn next-fallback [start-index {:keys [board]}]
  (first (filter #(fallback-space-available? board %)
                 (range (dec start-index) 0 -1))))

(defn player-has-card? [game-state card color]
  (pos? (get-in game-state [:players color :cards card])))

(defn square-has-color? [game-state color index]
  (pos? (get-in game-state [:board index :pieces color])))

(defn start-turn [game-state color]
  (assoc-in game-state [:players color :actions] 3))

(defn pass [game-state]
  (let [color (active-player-color game-state)]
    (start-turn
      (assoc-in game-state [:players color :actions] 0)
      (get-in game-state [:turn-order color]))))

(defn space-contents
  "Convert the pieces on the given space to a vector of pieces"
  ([space] (vec (reduce concat (map #(repeat (val %) (key %)) space))))
  ([game-state index]
   (space-contents (get-in game-state [:board index :pieces]))))

(defn hand-contents
  "Convert the player's cards (map of cards to count) to a vector of cards."
  ([hand] (vec (reduce concat (map #(repeat (val %) (key %)) hand))))
  ([game-state color]
   (hand-contents (get-in game-state [:players color :cards]))))

(defn move [game-state color from-index to-index]
  (-> game-state
      (update-in [:board from-index :pieces color] dec)
      (update-in [:board to-index :pieces color] inc)))

(defn use-action [game-state color]
  (-> game-state
      (update-in [:players color :actions] dec)
      (#(if (zero? (get-in % [:players color :actions]))
         (start-turn % (get-in % [:turn-order color])) %))))

(defn execute-play-card [game-state card-symbol pirate-color from-index]
  (let [to-index (next-open game-state (inc from-index) card-symbol)]
    (-> game-state
        (move pirate-color from-index to-index)
        (update-in [:players pirate-color :cards card-symbol] dec))))

#_(defn play-card [game-state card from-index]
  (let [color (active-player-color game-state)]
    (if (and (player-has-card? game-state card color)
           (square-has-color? game-state color from-index))
    (use-action (execute-play-card game-state card color from-index) color)
    game-state)))

(defn execute-fall-back [game-state from-index to-index pirate]
  (let [num-cards (piece-count (get-in game-state [:board to-index :pieces]))]
    (-> game-state
        (move pirate from-index to-index)
        (update-in [:players pirate :cards] #(apply cards->hand % (draw num-cards))))))

(defn fall-back [game-state start-index]
  (let [color (active-player-color game-state)
        to-index (next-fallback start-index game-state)]
    (if (and to-index (square-has-color? game-state color start-index))
      (use-action (execute-fall-back game-state start-index to-index color) color)
      game-state)))

(defmacro defaction [action-sym args w pred d action]
  `(defn ~action-sym ~args
     (let [game-state# ~(first args)
           color# (active-player-color game-state#)]
      (if (~pred color#)
        (use-action (~action color#) color#)
        game-state#))))

(macroexpand '(defaction play-card-action [game-state card from-index]
   :when #(and (player-has-card? game-state card %)
              (square-has-color? game-state % from-index))
   :do #(execute-play-card game-state card % from-index)))

(defaction play-card [game-state card from-index]
   :when #(and (player-has-card? game-state card %)
              (square-has-color? game-state % from-index))
   :do #(execute-play-card game-state card % from-index))

#_(defaction fall-back [game-state start-index]
   :when #(and to-index (square-has-color? game-state % start-index))
   :do #(execute-fall-back game-state start-index to-index %))
