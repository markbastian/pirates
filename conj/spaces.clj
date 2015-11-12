(defn next-open [{ :keys [board] :as game-state } start-index symbol]
  (let [r (range start-index (count board))]
    (or
      (first (filter #(and (not (space-occupied? game-state %))
                           (= symbol (get-in board [% :symbol]))) r))
      (dec (count board)))))

(defn next-fallback [start-index board]
  (first (filter #(space-available? board %) (range (dec start-index) 0 -1))))