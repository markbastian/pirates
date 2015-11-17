(ns pirates.pirates3d "A 3D version of Cartagena. Note that I just started this and nothing works yet. For now, look at
the swingui version."
  (:require [pirates.swingui :as ps])
  (:import (com.jme3.app SimpleApplication)
           (com.jme3.scene Geometry Node)
           (com.jme3.scene.shape Box)
           (com.jme3.material Material)
           (com.jme3.math ColorRGBA Vector3f)
           (com.jme3.asset AssetManager)
           (com.jme3.light DirectionalLight)))

(defn create-grid [^AssetManager am]
  (let [mat (doto (Material. am "Common/MatDefs/Light/Lighting.j3md")
              (.setBoolean "UseMaterialColors" true)
              (.setColor "Diffuse" ColorRGBA/Red)
              (.setColor "Specular" ColorRGBA/Red)
              (.setFloat "Shininess" (float 64.0)))
        box (Box. 0.5 0.5 0.1)]
    (for [[x y] ps/grid]
      (doto (Geometry. (str x "-" y) (.clone box))
        (doto (.setMaterial mat))
        (.setLocalTranslation x y 0)))))

;(.start
;  (let [state (atom {})]
;    (proxy [SimpleApplication] []
;    (simpleInitApp []
;      (let [am (.getAssetManager this)
;            path (create-grid am)]
;        (doto (.getRootNode this)
;          ((fn [root] (doseq [p path] (.attachChild ^Node root p))))
;          (.addLight (doto (DirectionalLight.)
;                       (.setDirection (.normalizeLocal (Vector3f. 1 0 -2)))
;                       (.setColor ColorRGBA/White))))))
;    (simpleUpdate [^double tpf]
;      (let [{:keys [geometry]} @state])))))
