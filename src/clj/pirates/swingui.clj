(ns pirates.swingui
  (:gen-class)
  (:require [pirates.rules :as rules]
            [clojure.java.io :as io]
            [clojure.pprint])
  (:import (java.awt Color BorderLayout Component Graphics2D)
           (javax.imageio ImageIO)
           (javax.swing JFrame JPopupMenu JMenuItem JOptionPane JLabel)
           (java.awt.geom Rectangle2D$Double Ellipse2D$Double)
           (java.awt.event MouseAdapter ActionListener MouseEvent)))

(defn load-image [s] (-> s io/resource io/input-stream ImageIO/read))

(def images
  { :start  (load-image "pirates/hands6.png")
    :boat   (load-image "pirates/sail1.png")
    :keys   (load-image "pirates/old45.png")
    :bottle (load-image "pirates/wine47.png")
    :flag   (load-image "pirates/halloween16.png")
    :hat    (load-image "pirates/fedora.png")
    :pistol (load-image "pirates/old3.png")
    :sword  (load-image "pirates/sword1.png")})

(def color-map
  { :red Color/RED :green Color/GREEN :blue Color/BLUE :yellow Color/YELLOW :brown (Color. 139 69 19) })

(defn grid [] [[0 0] [1 0] [2 0] [3 0] [4 0] [5 0] [6 0] [7 0]
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
  (let [x (* dim (first coord))
        y (* dim (second coord))]
    (Rectangle2D$Double. x y dim dim)))

(def track-shapes (vec (map #(create-square % 50) (grid))))

(def hand-shapes
  (zipmap
    (map #(create-square [9 %] 50) (range (count rules/card-types)))
    rules/card-types))

(defn draw-squares [board g]
  (doseq [i (range (count board))]
    (let [image ((get board i) images)
          shape (get track-shapes i)
          dx1 (.getMinX shape)
          dy1 (.getMinY shape)
          dx2 (.getMaxX shape)
          dy2 (.getMaxY shape)]
      (.draw g shape)
      (.drawImage g image dx1 dy1 dx2 dy2 0 0 (.getWidth image) (.getHeight image) nil))))

(defn centered-circle [cx cy r](Ellipse2D$Double. (- cx r) (- cy r) (* r 2) (* r 2)))

(defn draw-cards [player g]
  (let [color (color-map (key player))
        hand (:cards (val player))]
    (doseq [hand-shape hand-shapes]
      (let [shape (key hand-shape)
            card-type (val hand-shape)
            image (card-type images)
            dx1 (.getMinX shape)
            dy1 (.getMinY shape)
            dx2 (.getMaxX shape)
            dy2 (.getMaxY shape)]
        (.setPaint g color)
        (.draw g shape)
        (.drawImage g image dx1 dy1 dx2 dy2 0 0 (.getWidth image) (.getHeight image) nil)
        (.setPaint g Color/BLACK)
        (.drawString g (str (card-type hand)) (float (+ dx1 (.getWidth shape))) (float (+ dy1 (.getHeight shape))))
        ))))

;http://en.wikibooks.org/wiki/Clojure_Programming/Examples/API_Examples/Multimethod
;fn is a dispatch function using the same args as the actual function to be dispatched.
;Sort of a pre-eval'ed function.
(defmulti draw-piece (fn [pieces _ _] (count (rules/space-contents pieces))))

(defmethod draw-piece 0 [_ _ _])

(defmethod draw-piece 1 [pieces boundary g]
  (let [piece (first (rules/space-contents pieces))
        color (piece color-map)
        cx (.getCenterX boundary)
        cy (.getCenterY boundary)
        r (* (.getWidth boundary) 0.25)
        shape (centered-circle cx cy r)]
    (.setColor g color)
    (.fill g shape)))

(defmethod draw-piece 2 [pieces boundary g]
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

(defmethod draw-piece :default [pieces boundary g]
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
  ([pieces boundary g] (draw-piece pieces boundary g))
  ([pieces g]
   (doseq [i (range (count pieces))]
    (let [local-pieces (get pieces i)
          shape (get track-shapes i)]
       (draw-pieces local-pieces shape g)))))

(defn get-click-index [x y]
  (first (filter #(.contains (get track-shapes %) x y) (range (count track-shapes)))))

(defn play-card-action [parent game-state card color square-index]
  (proxy [ActionListener] []
    (actionPerformed [_]
      (dosync (ref-set game-state (rules/play-card card color square-index @game-state)))
      (.repaint parent)
      (if (rules/winner? @game-state color)
        (JOptionPane/showMessageDialog parent (str color " is the winner!"))))))

(defn fall-back-action [parent game-state color square-index]
  (proxy [ActionListener] []
    (actionPerformed [_]
      (dosync (ref-set game-state (rules/fall-back color square-index @game-state)))
      (.repaint parent))))

(defn pass-action [parent game-state color]
  (proxy [ActionListener] []
    (actionPerformed [_]
      (dosync (ref-set game-state (rules/pass color @game-state)))
      (.repaint parent))))

(defn popup [parent event cards game-state color square-index]
  (let [menu (JPopupMenu.)
        playable-cards (filter #(> (val %) 0) cards)
        play-card-buttons (map #(JMenuItem. (str "Play " %)) playable-cards)
        zm (zipmap playable-cards play-card-buttons)
        fall-back (JMenuItem. "Fall Back")
        pass (JMenuItem. "Pass")
        component (.getComponent event)
        x (.x (.getPoint event))
        y (.y (.getPoint event))]
    (doseq [c zm]
      (.addActionListener (val c) (play-card-action parent game-state (first (key c)) color square-index))
      (.add menu (val c)))
    (.addActionListener fall-back (fall-back-action parent game-state color square-index))
    (.add menu fall-back)
    (.addActionListener pass (pass-action parent game-state color))
    (.add menu pass)
    (if (.isPopupTrigger event) (.show menu component x y))))

(defn clicker [parent game-state]
  (proxy [MouseAdapter] []
    (mousePressed [event]
      (when-let [clicked (get-click-index (-> event .getPoint .x) (-> event .getPoint .y))]
        (let [player (rules/active-player @game-state)
              color (key player)]
          (popup parent event (get-in @game-state [:players color :cards]) game-state color clicked))))))

(defn c [game-state]
  (let [component (proxy [Component] []
                    (paint [g]
                      (draw-squares (get-in @game-state [:board :symbols]) g)
                      (draw-pieces (get-in @game-state [:board :pieces]) g)
                      (draw-cards (rules/active-player @game-state) g)))]
    (.addMouseListener component (clicker component game-state))
    component))

(defn frame [initial-game-state]
  (let [frame (JFrame. "Player Options")
        first-player (first (keys (:turn-order initial-game-state)))
        started (rules/start-turn first-player initial-game-state)
        game-state (ref started)]
    (doto frame
      (.setVisible true)
      (.setSize 800 600)
      (.add (c game-state) BorderLayout/CENTER)
      (.add (JLabel. "Right-click on square with piece to make a move.") BorderLayout/SOUTH)
      (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE))
    frame))

;(frame
;  (rules/init-game-state
;    #{{ :name "Mark" :color :green }
;      { :name "Bob" :color :yellow }
;      { :name "Gene" :color :blue }}))

(defn -main []
  (let [n (JOptionPane/showInputDialog nil "Enter players (2-5):")
        colors (take (read-string n) rules/pirate-colors)
        players (map #(partial {:color % :name (str % )}) colors)]
    (frame 
      (rules/init-game-state players))))