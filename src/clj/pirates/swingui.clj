(ns pirates.swingui
  (:gen-class)
  (:require [pirates.rules :as rules]
            [clojure.java.io :as io]
            [clojure.pprint :as pp])
  (:import (java.awt Color BorderLayout Component Graphics2D Shape)
           (javax.imageio ImageIO)
           (javax.swing JFrame JPopupMenu JMenuItem JOptionPane JLabel SwingUtilities)
           (java.awt.geom Rectangle2D$Double Ellipse2D$Double RectangularShape)
           (java.awt.event MouseAdapter ActionListener MouseEvent)))

(defn load-image [s] (-> s io/resource io/input-stream ImageIO/read))

(def images
  { :start (load-image "pirates/hands6.png")
   :boat   (load-image "pirates/sail1.png")
   :keys   (load-image "pirates/old45.png")
   :bottle (load-image "pirates/wine47.png")
   :flag   (load-image "pirates/halloween16.png")
   :hat    (load-image "pirates/fedora.png")
   :pistol (load-image "pirates/old3.png")
   :sword  (load-image "pirates/sword1.png")})

(def color-map
  { :red Color/RED :green Color/GREEN :blue Color/BLUE :yellow Color/YELLOW :brown (Color. 139 69 19) })

(def grid [[0 0] [1 0] [2 0] [3 0] [4 0] [5 0] [6 0] [7 0]
                                                     [7 1]
                                                     [7 2]
           [7 3] [6 3] [5 3] [4 3] [3 3] [2 3] [1 3] [0 3]
           [0 4]
           [0 5]
           [0 6] [1 6] [2 6] [3 6] [4 6] [5 6] [6 6] [7 6]
                                                     [7 7]
                                                     [7 8]
           [7 9] [6 9] [5 9] [4 9] [3 9] [2 9] [1 9] [0 9]])

(defn create-square [coord dim]
  (Rectangle2D$Double. (* dim (first coord)) (* dim (second coord)) dim dim))

(def track-shapes (mapv #(create-square % 50) grid))

(def hand-shapes
  (zipmap
    (map #(create-square [9 %] 50) (range (count rules/card-types)))
    rules/card-types))

(defn draw-image [g image shape]
  (.drawImage g image (.getMinX shape) (.getMinY shape) (.getMaxX shape) (.getMaxY shape)
              0 0 (.getWidth image) (.getHeight image) nil))

(defn draw-squares [^Graphics2D g board]
  (doseq [i (range (count board))]
    (let [image ((get-in board [i :symbol]) images)
          ^RectangularShape shape (get track-shapes i)]
      (doto g
        (.draw shape)
        (draw-image image shape)))))

(defn centered-circle [cx cy r](Ellipse2D$Double. (- cx r) (- cy r) (* r 2) (* r 2)))

(defn draw-cards [^Graphics2D g player]
  (let [color (color-map (key player))
        hand (:cards (val player))]
    (doseq [hand-shape hand-shapes]
      (let [shape (key hand-shape)
            card-type (val hand-shape)
            image (card-type images)
            dx1 (.getMinX shape)
            dy1 (.getMinY shape)]
        (doto g
          (.setPaint color)
          (.draw shape)
          (draw-image image shape)
          (.setPaint Color/BLACK)
          (.drawString (str (card-type hand)) (float (+ dx1 (.getWidth shape))) (float (+ dy1 (.getHeight shape)))))))))

(defmulti draw-piece (fn [_ pieces _] (count (rules/space-contents pieces))))

(defmethod draw-piece 0 [_ _ _])

(defmethod draw-piece 1 [^Graphics2D g pieces boundary]
  (let [piece (first (rules/space-contents pieces))
        color (piece color-map)
        cx (.getCenterX boundary)
        cy (.getCenterY boundary)
        r (* (.getWidth boundary) 0.25)
        shape (centered-circle cx cy r)]
    (.setColor g color)
    (.fill g shape)))

(defmethod draw-piece 2 [^Graphics2D g pieces boundary]
  (let [piece1 (first (rules/space-contents pieces))
        color1 (piece1 color-map)
        piece2 (second (rules/space-contents pieces))
        color2 (piece2 color-map)
        cx (.getCenterX boundary)
        cy (.getCenterY boundary)
        r (/ (.getWidth boundary) 3.0)]
    (.setColor g color1)
    (.fill g (Ellipse2D$Double. (- cx (* r 0.5)) (+ (- cy (* r 0.5)) (/ r 2.0)) r r))
    (.setColor g color2)
    (.fill g (Ellipse2D$Double. (- cx (* r 0.5)) (- (- cy (* r 0.5)) (/ r 2.0)) r r))))

(defmethod draw-piece :default [^Graphics2D g pieces boundary]
  (let [p (rules/space-contents pieces)
        n (count p)
        cx (.getCenterX boundary)
        cy (.getCenterY boundary)
        r (* 3.0 (Math/sqrt (.getWidth boundary)))
        theta (/ Math/PI (inc n) 0.5)
        cl (* 2.0 r (Math/sin (* theta 0.5)))]
    (doseq [i (range n)]
      (let [piece (p i)
            color (piece color-map)
            y (* r (Math/sin (* i theta)))
            x (* r (Math/cos (* i theta)))
            shape (Ellipse2D$Double. (- cx x (* cl 0.5)) (- cy y (* cl 0.5)) cl cl)]
        (.setColor g color)
        (.fill g shape)))))

(defn draw-pieces
  ([^Graphics2D g pieces boundary] (draw-piece g pieces boundary))
  ([^Graphics2D g pieces]
   (doseq [i (range (count pieces))]
    (let [local-pieces (get-in pieces [i :pieces])
          shape (get track-shapes i)]
       (draw-pieces g local-pieces shape)))))

(defn get-click-index [x y]
  (first (filter #(.contains ^Shape (get track-shapes %) x y) (range (count track-shapes)))))

(defn create-action [parent action]
  (proxy [ActionListener] []
    (actionPerformed [_]
      (action)
      (.repaint parent))))

(defn popup [parent ^MouseEvent event cards game-state square-index]
  (let [playable-cards (filter #(> (val %) 0) cards)
        f (fn [menu]
            (doseq [c (zipmap playable-cards (map #(JMenuItem. (str "Play " %)) playable-cards))]
              (.addActionListener (val c) (create-action parent #(swap! game-state rules/play-card (first (key c)) square-index)))
              (.add menu (val c))))]
    (if (.isPopupTrigger event)
      (.show (doto (JPopupMenu.)
               f
               (.add (doto (JMenuItem. "Fall Back") (.addActionListener (create-action parent #(swap! game-state rules/fall-back square-index)))))
               (.add (doto (JMenuItem. "Pass") (.addActionListener (create-action parent #(swap! game-state rules/pass))))))
             (.getComponent event) (.x (.getPoint event)) (.y (.getPoint event))))))

(defn clicker [parent game-state]
  (let [click-handler #(when-let [clicked (get-click-index (-> % .getPoint .x) (-> % .getPoint .y))]
                        (let [color (key (rules/active-player @game-state))]
                          (popup parent % (get-in @game-state [:players color :cards]) game-state clicked)))]
    (proxy [MouseAdapter] []
      (mousePressed [event] (click-handler event))
      (mouseReleased [event] (click-handler event)))))

(defn c [game-state]
  (let [component (proxy [Component] []
                    (paint [g]
                      (doto g
                        (draw-squares (:board @game-state))
                        (draw-pieces (:board @game-state))
                        (draw-cards (rules/active-player @game-state)))))]
    (.addMouseListener component (clicker component game-state))
    component))

(defn setup-winner-watch [state]
  (add-watch
    state :watch-for-winner
             (fn [_ _ _ n]
               (if (rules/winner? n)
                 (SwingUtilities/invokeLater
                   (JOptionPane/showMessageDialog
                     nil (str (key (rules/active-player n)) " is the winner!")))))))

;(defn almost-win [state]
;  (-> state
;      (assoc-in [:board 0 :pieces :yellow] 0)
;      (assoc-in [:board 36 :pieces :yellow] 1)
;      (assoc-in [:board 37 :pieces :yellow] 5)
;      (doto pp/pprint)))

(defn frame [initial-game-state exit-condition]
  (let [frame (JFrame. "Pirates!")
        first-player (first (keys (:turn-order initial-game-state)))
        started (rules/start-turn initial-game-state first-player)
        ;game-state (atom (almost-win started))
        game-state (atom started)
        ]
    (setup-winner-watch game-state)
    (doto frame
      (.setSize 600 600)
      (.add ^Component (c game-state) BorderLayout/CENTER)
      (.add (JLabel. "Right-click on square with piece to make a move.") BorderLayout/SOUTH)
      (.setDefaultCloseOperation exit-condition)
      (.setVisible true)
      (.repaint))
    frame))

;Uncomment and load this in the REPL if you don't want to run via lein run.
(frame
  (rules/init-game-state
    #{{ :name "Mark" :color :green }
      { :name "Bob" :color :yellow }
      { :name "Gene" :color :blue }})
  JFrame/DISPOSE_ON_CLOSE)

(defn -main []
  (let [n (JOptionPane/showInputDialog nil "Enter players (2-5):")
        colors (take (read-string n) rules/pirate-colors)
        players (map #(partial {:color % :name (str % )}) colors)]
    (frame 
      (rules/init-game-state players)
      JFrame/EXIT_ON_CLOSE)))
