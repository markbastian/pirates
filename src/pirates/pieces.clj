(ns pirates.pieces)

;The potential cards you can get.
(def card-types #{:hat :flag :pistol :sword :bottle :keys})

;Pawn types - id'd by color.
(def pirate-colors #{:red :green :blue :yellow :brown})

(defn create-pirates [color](map #(partial { :color color :index %}) (range 6)))

(def pieces (zipmap pirate-colors (map create-pirates pirate-colors)))
