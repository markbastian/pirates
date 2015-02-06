(ns pirates.swingui
  (:gen-class)
  (:require [pirates.actions :as actions]
            [clojure.java.io :as io]
            [clojure.pprint])
  (:import (java.awt Color BorderLayout Component Graphics2D)
           (javax.imageio ImageIO)
           (javax.swing JFrame JPopupMenu JMenuItem JOptionPane)
           (java.awt.geom Rectangle2D$Double Ellipse2D$Double)
           (java.awt.event MouseAdapter ActionListener MouseEvent)))

(def images
  { :start  (ImageIO/read (io/input-stream "hands6.png"))
    :boat   (ImageIO/read (io/input-stream "sail1.png"))
    :keys   (ImageIO/read (io/input-stream "key142.png"))
    :bottle (ImageIO/read (io/input-stream "wine47.png"))
    :flag   (ImageIO/read (io/input-stream "halloween16.png"))
    :hat    (ImageIO/read (io/input-stream "fedora.png"))
    :pistol (ImageIO/read (io/input-stream "gun1.png"))
    :sword  (ImageIO/read (io/input-stream "sword1.png"))})

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
    (map #(create-square [9 %] 50) (range (count actions/card-types)))
    actions/card-types))

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
(defmulti draw-piece (fn [pieces _ _] (count (actions/space-contents pieces))))

(defmethod draw-piece 0 [_ _ _])

(defmethod draw-piece 1 [pieces boundary g]
  (let [piece (first (actions/space-contents pieces))
        color (piece color-map)
        cx (.getCenterX boundary)
        cy (.getCenterY boundary)
        r (* (.getWidth boundary) 0.25)
        shape (centered-circle cx cy r)]
    (.setColor g color)
    (.fill g shape)))

(defmethod draw-piece 2 [pieces boundary g]
  (let [piece1 (first (actions/space-contents pieces))
        color1 (piece1 color-map)
        piece2 (second (actions/space-contents pieces))
        color2 (piece2 color-map)
        cx (.getCenterX boundary)
        cy (.getCenterY boundary)
        r (/ (.getWidth boundary) 3.0)]
    (.setColor g color1)
    (.fill g (Ellipse2D$Double. (- cx (* r 0.5)) (+ (- cy (* r 0.5)) (/ r 2.0)) r r))
    (.setColor g color2)
    (.fill g (Ellipse2D$Double. (- cx (* r 0.5)) (- (- cy (* r 0.5)) (/ r 2.0)) r r))))

(defmethod draw-piece :default [pieces boundary g]
  (let [p (actions/space-contents pieces)
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
      (dosync (ref-set game-state (actions/play-card card color square-index @game-state)))
      (.repaint parent)
      (if (actions/winner? @game-state color)
        (JOptionPane/showMessageDialog parent (str color " is the winner!"))))))

(defn fall-back-action [parent game-state color square-index]
  (proxy [ActionListener] []
    (actionPerformed [_]
      (dosync (ref-set game-state (actions/fall-back color square-index @game-state)))
      (.repaint parent))))

(defn pass-action [parent game-state color]
  (proxy [ActionListener] []
    (actionPerformed [_]
      (dosync (ref-set game-state (actions/pass color @game-state)))
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
        (let [player (actions/active-player @game-state)
              color (key player)]
          (popup parent event (get-in @game-state [:players color :cards]) game-state color clicked))))))

(defn c [game-state]
  (let [component (proxy [Component] []
                    (paint [g]
                      (draw-squares (get-in @game-state [:board :symbols]) g)
                      (draw-pieces (get-in @game-state [:board :pieces]) g)
                      (draw-cards (actions/active-player @game-state) g)))]
    (.addMouseListener component (clicker component game-state))
    component))

(defn frame [initial-game-state]
  (let [frame (JFrame. "Player Options")
        first-player (first (keys (:turn-order initial-game-state)))
        started (actions/start-turn first-player initial-game-state)
        game-state (ref started)]
    (doto frame
      (.setVisible true)
      (.setSize 800 600)
      (.add (c game-state) BorderLayout/CENTER)
      (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE))
    frame))

(frame
  (actions/init-game-state
    #{{ :name "Mark" :color :green }
      { :name "Bob" :color :yellow }
      { :name "Gene" :color :blue }}))

(defn -main []
  (let [n (JOptionPane/showInputDialog nil "Enter players (2-5):")
        colors (take (read-string n) actions/pirate-colors)
        players (map #(partial {:color % :name (str % )}) colors)]
    (frame (actions/init-game-state players))))