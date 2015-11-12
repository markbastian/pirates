(defn cards->hand
  ([hand card](update-in hand [card] inc))
  ([hand card & more](reduce #(cards->hand %1 %2)
                             (cards->hand hand card) more)))

(def empty-hand (zipmap card-types (repeat 0)))

(defn initial-hand []
  (apply cards->hand empty-hand (draw 6)))

(defn init-players [players]
  (zipmap
    (map :color players)
    (map #(into {} { :name (:name %)
                    :cards (initial-hand)
                    :actions 0 })
         players)))