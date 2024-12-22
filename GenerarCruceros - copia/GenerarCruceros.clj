(ns SemaforoClojure.GenerarCruceros.GenerarCruceros
  (:require [clojure.java.io :as io]))

(defn generar-vehiculos []
  (let [tiempos (sort (repeatedly (+ 2 (rand-int 15)) #(rand-int 201)))]
    (mapv (fn [t] t) tiempos)));Cada vehiculo tiene un tiempo de llegada

(defn generar-semaforo [id]
  [id (+ 5 (rand-int 11)) (+ 5 (rand-int 11)) (generar-vehiculos)]);id, tiempo-verde, tiempo-rojo, vehiculos

(defn generar-crucero [id]
  [id (mapv #(generar-semaforo %) (range 1 (+ 2 (inc (rand-int 3)))))]) ;id, semaforos

(defn escribir-archivo [nombre-contenido]
  (doseq [[nombre contenido] nombre-contenido]
    (with-open [w (io/writer nombre)]
      (binding [*out* w]
        (prn contenido)))))

(defn -main []
  (let [cruceros (mapv generar-crucero (range 1 200))
        ciudad-config (mapv first cruceros)];Se obtiene el id de cada crucero
    (escribir-archivo (conj (map (fn [c] [(str "crucero_" (first c) ".txt") c]) cruceros)
                            ["ciudad_config.txt" ciudad-config]))))

;; Ejecuta la función principal
(-main)
