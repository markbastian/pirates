(ns pirates.swingui
  (:require [pirates.pieces :as pieces])
  (:import (javax.swing JDialog JLabel JTextField JComboBox JFrame)
           (java.awt GridBagConstraints GridBagLayout)))

(defn panel [owner]
  (let [p (JDialog. owner "Enter Player Information" true)
        name-label (JLabel. "Name:")
        name-field (JTextField. "Player")
        color-label (JLabel. "Color:")
        color-field (JComboBox. (object-array pieces/pirate-colors))
        c (GridBagConstraints.)]
    (.setLayout p (GridBagLayout.))
    (set! (.fill c) GridBagConstraints/HORIZONTAL)
    (set! (.weightx c) 0.5)
    (set! (.gridx c) 0)
    (set! (.gridy c) 0)
    (.add p name-label c)
    (set! (.gridx c) 0)
    (set! (.gridy c) 1)
    (.add p color-label c)
    (set! (.gridx c) 1)
    (set! (.gridy c) 0)
    (.add p name-field c)
    (set! (.gridx c) 1)
    (set! (.gridy c) 1)
    (.add p color-field c)
    (.setVisible p true)))

(defn frame []
  (let [frame (JFrame. "Player Options")]
    (doto frame
      (.setVisible true)
      (.setSize 800 600))
    (panel frame)
    frame))
