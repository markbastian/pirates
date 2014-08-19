(ns pirates.game
  (:require
    [pirates.setup  :as setup]
    [pirates.actions :as actions]
    [pirates.consoleui :as ui])
  (:gen-class :name pirates.game))

;Board example usage functions
(def player-prefs #{{ :name "Mark" :color :green }
               { :name "Bob" :color :yellow }
               { :name "Gene" :color :blue }})

;(def board (setup/init-board (map :color player-prefs)))
(def board (setup/gen-board))
(def piece-places (setup/init-pieces (map :color player-prefs) board))
(def players (setup/init-players player-prefs))

(println "The board: " board)
(println "The players: " players)
(println "The pieces: " piece-places)

(println "pieces at start: " (get piece-places 0))
(println "pieces somewhere else: " (get piece-places 4))
(println "pieces off board: " (get piece-places 400))

(println "space 0 available: " (actions/space-available? piece-places 0))
(println "space 4 available: " (actions/space-available? piece-places 4))
(println "space 400 available: " (actions/space-available? piece-places 400))

(println "space 0 occupied: " (actions/space-occupied? piece-places 0))
(println "space 4 occupied: " (actions/space-occupied? piece-places 4))
(println "space 400 occupied: " (actions/space-occupied? piece-places 400))

(def test-pirate {:color :blue, :index 0})
(println "test pirate at origin: " (actions/pirate-on-space? test-pirate piece-places 0))
(println "test pirate not somewhere else: " (actions/pirate-on-space? test-pirate piece-places 3))
(println "test pirate not off board: " (actions/pirate-on-space? test-pirate piece-places 60))

(println "Where is the pirate?: " (actions/find-pirate test-pirate piece-places))
(println "Where is the pirate?: " (actions/find-pirate :feas piece-places))

(println "hats are at: " (actions/symbol-indices :hat board))
(println "pistols are at: " (actions/symbol-indices :pistol board))

(println "Next available from 0 is: " (actions/next-open 0 :hat board piece-places))AbstractMethodError
(println "Next available from 33 is: " (actions/next-open 15 :hat board piece-places))
(println "Next available from 36 is: " (actions/next-open 36 :hat board piece-places))

(println "play :hat " (actions/play-card :hat test-pirate board piece-places))
AbstractMethodError
(def piece-places-1 (actions/play-card :hat (first (get piece-places 0)) board piece-places))

(println "######################################################################### " (first (get piece-places 0)) " to :hat #########################################################################")
(ui/print-board board piece-places-1)

(def piece-places-2 (actions/play-card :sword (first (get piece-places-1 0)) board piece-places-1))
(println "#########################################################################" (first (get piece-places-1 0)) " to :sword #########################################################################")
(ui/print-board board piece-places-2)

(def piece-places-3 (actions/play-card :sword (first (get piece-places-2 0)) board piece-places-2))
(println "#########################################################################" (first (get piece-places-2 0)) " to :sword #########################################################################")
(ui/print-board board piece-places-3)

(println "######################################################################### fall back! #########################################################################")
(def fb (actions/fall-back {:color :blue, :index 5} piece-places-3 players))
(ui/print-board board (first fb))




