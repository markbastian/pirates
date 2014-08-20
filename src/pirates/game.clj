(ns pirates.game
  (:require
    [pirates.setup  :as setup]
    [pirates.actions :as actions]
    [pirates.consoleui :as ui])
  (:gen-class :name pirates.game))

;Board example usage functions
(def player-prefs
  #{{ :name "Mark" :color :green }
    { :name "Bob" :color :yellow }
    { :name "Gene" :color :blue }})

(def game-state
  { :players (setup/init-players player-prefs)
    :board (setup/make-board (map :color player-prefs))
    :turn-order (vec (map :color player-prefs))})

(println "pieces at start: " (get-in game-state [:board :pieces 0]))
(println "pieces somewhere else: " (get-in game-state [:board :pieces 4]))
(println "pieces off board: " (get-in game-state [:board :pieces 400]))

(println "space 0 available: " (actions/space-available? (get-in game-state [:board :pieces 0])))
(println "space 4 available: " (actions/space-available? (get-in game-state [:board :pieces]) 4))
(println "space 400 available: " (actions/space-available? (get-in game-state [:board :pieces]) 400))

(println "space 0 occupied: " (actions/space-occupied? game-state 0))
(println "space 4 occupied: " (actions/space-occupied? game-state 4))
(println "space 400 occupied: " (actions/space-occupied? game-state 400))

(println "test pirate at origin: " (actions/pirate-on-space? :blue game-state 0))
(println "test pirate not somewhere else: " (actions/pirate-on-space? :blue game-state 3))
(println "test pirate not off board: " (actions/pirate-on-space? :blue game-state 60))

(println "Where are the pirates?: " (actions/find-pirates :blue game-state))
(println "Where are the pirates?: " (actions/find-pirates :feas game-state))

(println "hats are at: " (actions/symbol-indices :hat (get-in game-state [:board :symbols])))
(println "pistols are at: " (actions/symbol-indices :pistol (get-in game-state [:board :symbols])))

(println "Next available hat/boat from 0 is: " (actions/next-open 0 :hat (get-in game-state [:board])))
(println "Next available hat/boat from 33 is: " (actions/next-open 15 :hat (get-in game-state [:board])))
(println "Next available hat/boat from 36 is: " (actions/next-open 36 :hat (get-in game-state [:board])))

(println "blue plays first card")
(def game-state-1
  (actions/play-card
    (key (first (actions/available-cards game-state :blue))) :blue 0 game-state))
(clojure.pprint/pprint game-state-1)

(println "blue plays second card")
(def game-state-2
  (actions/play-card
    (key (last (actions/available-cards game-state-1 :blue))) :blue 0 game-state-1))
(clojure.pprint/pprint game-state-2)

(def n (last (actions/find-pirates :blue game-state-2)))
(def to-n (actions/next-fallback n (get-in game-state-2 [:board :pieces])))
(println "blue falls back from " n " to " to-n)
(def game-state-3 (actions/fall-back :blue n game-state-2))
(clojure.pprint/pprint game-state-3)

(defn play-first-card [color game-state]
  (actions/play-card (key (first (actions/available-cards game-state color))) color 0 game-state))

(println "Taking a full turn!!!!!")
(def went (actions/take-turn :blue game-state play-first-card))
(clojure.pprint/pprint went)
(def went-again (actions/take-turn :blue went play-first-card))
(clojure.pprint/pprint went-again)