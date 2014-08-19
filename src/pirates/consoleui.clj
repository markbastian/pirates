(ns pirates.consoleui)

(defn get-action []
  ((println "Select Action:")
   (println "Enter space # to fall back or type card then space # to advance:")
   (read-line)))

(defn print-board [board pieces]
  (doseq [entry (map #(vec [%1 %2]) board pieces)]
    (println entry)))
