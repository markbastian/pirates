(defn add-initial-pieces [board colors]
  (assoc-in board [0 :pieces] (zipmap colors (repeat 6))))

(defn make-board [used-colors]
  (let [empty-color-map { :pieces (zipmap used-colors (repeat 0)) }]
    (mapv #(assoc empty-color-map :symbol %) (gen-board-sequence))))

(defn setup-board [colors] (add-initial-pieces (make-board colors) colors))