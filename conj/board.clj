(def card-types #{:hat :flag :pistol :sword :bottle :keys})

(defn gen-board-sequence []
      (flatten [:start
                (repeatedly 6 #(shuffle card-types))
                :boat]))

