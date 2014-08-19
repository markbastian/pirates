(ns pirates.generators
  (:import (javax.swing JComponent Box BoxLayout JFileChooser JFrame JLabel JTextField JComboBox JComponent JOptionPane
                        JPanel JDialog)
           (java.awt GridBagConstraints GridBagLayout))
  (:require
    [pirates.pieces :as pieces]
    [pirates.actions :as actions]))

(def start-tile {:symbol :start})
(def end-tile {:symbol :boat})

(defn selected-colors [players] (set (map :color (vals (:players players)))))
(defn available-colors [players] (remove (set (selected-colors players)) pieces/pirate-colors))

(defn create-player
  ([state new-player] ;Create a player by name and token selection with no starting players.
   (let[players (state :players)
        available-colors (state :available-colors)
        name (new-player :name)
        color (new-player :color)]
     (if (and
           (available-colors color)
           (not (contains? players name)))
       {:players (into players {name {:color color :cards (vec (actions/draw 5))}})
        :available-colors (disj available-colors color)}
       state))
   )
  ([state new-player & more] (reduce create-player (create-player state new-player) more)))

