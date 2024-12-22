(ns SemaforoClojure.SemaforoClojure.SemaforoClojure 
  (:require [clojure.java.io :as io]))

;; ============================================================
;;                       Inicialización
;; ============================================================
; Inicializará los semáforos con valores predeterminados para todos los campos excepto los que se leen del archivo de configuración.
(defn inicializar-semaforos [semaforos]
  (map (fn [sem]
         (assoc sem
           4 0 ;tiempo-actual
           5 :verde ;estado
           6 0 ;ultimo-cruce
           7 [] ;vehiculos-pueden-pasar
           8 0 ;vehiculos-pasados
           9 0 ;tiempo-en-pasar
           10 0));tiempo-muerto
       semaforos))

;; ============================================================
;;                        Utilidades
;; ============================================================
; Borra todos los archivos de logs de ejecuciones pasadas
(defn borrar-logs []
  (doseq [file (file-seq (io/file "."))
          :when (re-matches #"crucero_.*_log\.txt" (.getName file))]
    (io/delete-file file)))

(defn borrar-estadisticas []
  (doseq [file (file-seq (io/file "."))
          :when (re-matches #"crucero_.*_estadistica\.txt" (.getName file))]
    (io/delete-file file)))

(defn borrar-estadisticaCiudad []
  (doseq [file (file-seq (io/file "."))
          :when (re-matches #"ciudad_estadistica\.txt" (.getName file))]
    (io/delete-file file)))

(defn borrar-archivos []
  (borrar-logs)
  (borrar-estadisticas)
  (borrar-estadisticaCiudad))

; Función para imprimir en consolar y registrar logs de un crucero específico.
(defn imprimirYRegistrarLog [crucero-id cadena imprimirConsola & argumentos]
  (when imprimirConsola
    (println (apply format cadena argumentos)))
  (with-open [w (io/writer (str "crucero_" crucero-id "_log.txt") :append true)]
    (.write w (str (apply format cadena argumentos)))
    (.write w (str "\n"))))

; Función para imprimir en consolar y registrar estadísticas de un crucero específico.
(defn imprimirYRegistrarEstadistica [crucero-id cadena imprimirConsola & argumentos]
  (when imprimirConsola
    (println (apply format cadena argumentos)))
  (with-open [w (io/writer (str "crucero_" crucero-id "_estadistica.txt") :append true)]
    (.write w (str (apply format cadena argumentos)))
    (.write w (str "\n"))))

; Función para imprimir en consolar y registrar estadísticas de una ciudad.
(defn imprimirYRegistrarEstadisticaCiudad [cadena imprimirConsola & argumentos]
  (when imprimirConsola
    (println (apply format cadena argumentos)))
  (with-open [w (io/writer "ciudad_estadistica.txt" :append true)]
    (.write w (str (apply format cadena argumentos)))
    (.write w (str "\n"))))

;; ============================================================
;;                Funciones de Lectura y Escritura
;; ============================================================
; Función para leer archivo de configuraciones
(defn leer-archivo [archivo]
  (with-open [r (io/reader archivo)]
    (read-string (slurp r))))

; Función para reescribir los semáforos de un crucero
(defn reescribir-semaforos [crucero-id semaforos]
  (let [archivo (str "crucero_" crucero-id "_mutabilidad.txt")]
    (with-open [w (io/writer archivo)]
      (binding [*out* w]
        (prn semaforos)))))

; Función para obtener todos los semáforos de un crucero
(defn obtener-semaforos-crucero [crucero-id]
  (leer-archivo (str "crucero_" crucero-id "_mutabilidad.txt")))

;; ============================================================
;;                   Cálculo de estadísticas
;; ============================================================
(defn calcular-estadisticas-semaforo [semaforo crucero-id]
  [
    (nth semaforo 0) ;id
    crucero-id ;crucero-id
    (nth semaforo 8) ;vehiculos-pasados
    (nth semaforo 9) ;tiempo-en-pasar
    (if (pos? (nth semaforo 8))
      (/ (nth semaforo 9) (nth semaforo 8))
      0) ;tiempo-promedio
    (nth semaforo 10) ;tiempo-muerto
    ])

(defn calcular-estadisticas-crucero [crucero-id]
  (let [semaforos (obtener-semaforos-crucero crucero-id)
        estadisticas-semaforos (map #(calcular-estadisticas-semaforo % crucero-id) semaforos)
        total-vehiculos (reduce + (map #(nth % 2) estadisticas-semaforos))
        total-tiempo-en-pasar (reduce + (map #(nth % 3) estadisticas-semaforos))
        total-tiempo-muerto (reduce +  (map #(nth % 5) estadisticas-semaforos))
        tiempo-promedio (if (pos? total-vehiculos) (/ total-tiempo-en-pasar total-vehiculos) 0)]
    [
     crucero-id ;id
     estadisticas-semaforos ;semaforos
     total-vehiculos ;total-vehiculos
     tiempo-promedio ;tiempo-promedio
     total-tiempo-muerto ;tiempo-muerto
     ]))

(defn imprimir-estadisticas-semaforo [crucero-id sem]
  (imprimirYRegistrarEstadistica crucero-id "Semáforo %d:" false (nth sem 0))
  (imprimirYRegistrarEstadistica crucero-id "- Vehículos pasados: %d" false (nth sem 2))
  (imprimirYRegistrarEstadistica crucero-id "- Tiempo promedio: %f segundos" false (double (nth sem 4)))
  (imprimirYRegistrarEstadistica crucero-id "- Tiempo muerto: %d segundos" false (nth sem 5)))

(defn imprimir-estadisticas-crucero [estadisticas]
  (imprimirYRegistrarEstadistica (nth estadisticas 0) "Estadísticas del Crucero %d:" false (nth estadisticas 0))
  (imprimirYRegistrarEstadistica (nth estadisticas 0) "Total de vehículos: %d" false (nth estadisticas 2))
  (imprimirYRegistrarEstadistica (nth estadisticas 0) "Tiempo promedio de espera: %f segundos" false (double (nth estadisticas 3)))
  (imprimirYRegistrarEstadistica (nth estadisticas 0) "Tiempo muerto total: %d segundos" false (nth estadisticas 4))
  (dorun (map #(imprimir-estadisticas-semaforo (nth estadisticas 0) %) (nth estadisticas 1)))
  (imprimirYRegistrarEstadistica (nth estadisticas 0) "Las estadísticas del crucero %d fueron terminadas." true (nth estadisticas 0)))

(defn calcular-estadisticas-ciudad []
  (let [cruceros (leer-archivo "ciudad_config.txt")
        estadisticas-cruceros (map calcular-estadisticas-crucero cruceros)
        top-10-mayor-tiempo (take (Math/ceil (* 0.1 (count estadisticas-cruceros)))
                                  (sort-by #(nth % 3) > estadisticas-cruceros))
        top-10-menor-tiempo (take (Math/ceil (* 0.1 (count estadisticas-cruceros)))
                                  (sort-by #(nth % 3) estadisticas-cruceros))
        semaforos (mapcat #(nth % 1) estadisticas-cruceros)
        top-10-semaforos-muertos (take (Math/ceil (* 0.1 (count semaforos)))
                                       (sort-by #(nth % 4) > semaforos))]
  
    [
     estadisticas-cruceros ;cruceros
     top-10-mayor-tiempo ;top-10-mayor-tiempo
     top-10-menor-tiempo ;top-10-menor-tiempo
     top-10-semaforos-muertos ;top-10-semaforos-muertos
    ]))

(defn imprimir-estadisticas-ciudad []
  ; Calculamos las estadísticas de cada crucero individualmente
  (dorun
    (pmap (fn [crucero-part]
            (dorun (map #(imprimir-estadisticas-crucero (calcular-estadisticas-crucero %))
                        crucero-part)))
          (partition-all 10 (doall (leer-archivo "ciudad_config.txt")))))
  
  ; Calculamos las estadísticas generales de la ciudad sin paralelismo
  (comment
    (dorun (map #(imprimir-estadisticas-crucero (calcular-estadisticas-crucero %))
                (leer-archivo "ciudad_config.txt"))))

  ; Calculamos las estadísticas generales
  (let [estadisticas (calcular-estadisticas-ciudad)]
    (imprimirYRegistrarEstadisticaCiudad "Estadísticas de la Ciudad:" true)
    (imprimirYRegistrarEstadisticaCiudad "Cruceros:" true)
    (dorun (map #(imprimirYRegistrarEstadisticaCiudad " - Crucero %d: %d semáforos, %d vehículos pasados" true
                                                      (nth % 0) (count (nth % 1)) (nth % 2))
                (nth estadisticas 0)))

    (imprimirYRegistrarEstadisticaCiudad "Tiempo promedio de espera de todos los vehículos: %f segundos" true
                                         (if (pos? (reduce + (map #(nth % 2) (nth estadisticas 0))))
                                           (double (/ (reduce + (map #(nth % 3) (nth estadisticas 0)))
                                                      (reduce + (map #(nth % 2) (nth estadisticas 0)))))
                                           0))
    
    (imprimirYRegistrarEstadisticaCiudad "Top 10%% Cruceros con Mayor Tiempo de Espera:" true)
    (dorun (map #(imprimirYRegistrarEstadisticaCiudad "Crucero %d: %f segundos" true (nth % 0) (double (nth % 3)))
                (nth estadisticas 1)))

    (imprimirYRegistrarEstadisticaCiudad "Top 10%% Cruceros con Menor Tiempo de Espera:" true)
    (dorun (map #(imprimirYRegistrarEstadisticaCiudad "Crucero %d: %f segundos" true (nth % 0) (double (nth % 3)))
                (nth estadisticas 2)))

    (imprimirYRegistrarEstadisticaCiudad "Top 10%% Semáforos con Mayor Tiempo Muerto:" true)
    (dorun (map #(imprimirYRegistrarEstadisticaCiudad "Semáforo %d del Crucero %d: %d segundos" true
                                                      (nth % 0) (nth % 1) (nth % 5))
                (nth estadisticas 3)))))

;; ============================================================
;;                   Lógica del Semáforo
;; ============================================================
; Funciones para Actualizar el Estado de los Semáforos:
(defn actualizar-estado-semaforo [sem]
  (cond
    (and (= :verde (nth sem 5)) (< (nth sem 4) (nth sem 1)))
    (assoc sem 
           4 (inc (nth sem 4)))

    (and (= :verde (nth sem 5)) (= (nth sem 4) (nth sem 1)))
    (assoc sem 
           4 1 
           5 :rojo)

    (and (= :rojo (nth sem 5)) (< (nth sem 4) (nth sem 2)))
    (assoc sem 
           4 (inc (nth sem 4)))

    (and (= :rojo (nth sem 5)) (= (nth sem 4) (nth sem 2)))
    (assoc sem 
           4 1 
           5 :verde)))

(comment (defn actualizar-semaforos [semaforos]
           (map actualizar-estado-semaforo semaforos)))

; Función para imprimir los vehículos restantes de cada semáforo de un crucero
(defn imprimir-vehiculos-restantes-crucero [crucero-id]
  (imprimirYRegistrarLog crucero-id "-------------------------" false)
  (imprimirYRegistrarLog crucero-id "Vehículos restantes:" false)
  (doseq [sem (obtener-semaforos-crucero crucero-id)]
    (imprimirYRegistrarLog crucero-id "Semáforo %d: %s" false (nth sem 0) (pr-str (nth sem 3))))
  )

; Función que imprime el estado actual de los semáforos de un crucero
(defn imprimir-estado-semaforos [crucero-id semaforos tiempo-actual]
  (imprimirYRegistrarLog crucero-id "Estado de semáforos:" false)
  (doseq [sem semaforos]
    (imprimirYRegistrarLog crucero-id "Semáforo %d: %s en tiempo %d" false
                           (nth sem 0) (name (nth sem 5)) tiempo-actual)))

;; ============================================================
;;                     Funciones de Vehículos
;; ============================================================
; Calcula que vehículos acaban de llegar a un semáforo especifico, pero aún no cruzan
(defn calcular-vehiculos-llegan-ahora [vehiculos tiempo-actual]
  (filter #(= % tiempo-actual) vehiculos))

; Adjunta los vehículos que llegaron a la lista de vehículos que van a pasar por el semáforo, más adelante cruzarán.
(defn calcular-vehiculos-van-pasar [semaforo vehiculos-llegan-ahora]
  (if (or (nil? vehiculos-llegan-ahora) (empty? vehiculos-llegan-ahora))
    (nth semaforo 7)
    (concat (nth semaforo 7) vehiculos-llegan-ahora)))

; Calcula el vehículo que están cruzando en ese momento el semáforo, solo puede pasar uno.
(defn calcular-vehiculo-cruzando [semaforo tiempo-actual tiempo-cruce]
  (if (and (not-empty (nth semaforo 7))
           (>= tiempo-actual (+ (nth semaforo 6) tiempo-cruce))
           (>= tiempo-actual (+ (first (nth semaforo 7)) tiempo-cruce)))
    [(first (nth semaforo 7))]
    []))

; Procesa los vehículos para un semáforo en un instante de tiempo determinado
(defn procesar-vehiculos [crucero-id sem tiempo-actual tiempo-cruce]
  (let [vehiculos-llegan-ahora (calcular-vehiculos-llegan-ahora (nth sem 3) tiempo-actual)
        vehiculos-pueden-pasar (calcular-vehiculos-van-pasar sem vehiculos-llegan-ahora)
        vehiculo-cruzando (calcular-vehiculo-cruzando sem tiempo-actual tiempo-cruce)
        vehiculos-actualizados (if (not-empty vehiculo-cruzando)
                                 (remove #(= % (first vehiculo-cruzando)) (nth sem 3))
                                 (nth sem 3))
        vehiculos-pasados-actualizados (if (not-empty vehiculo-cruzando) (inc (nth sem 8)) (nth sem 8))
        tiempo-en-pasar-actualizado (if (not-empty vehiculo-cruzando) (+ (nth sem 9) (- tiempo-actual  (first vehiculo-cruzando))) (nth sem 9))
        tiempo-muerto-actualizado (if (and (= :verde (nth sem 5)) (empty? vehiculos-pueden-pasar) (empty? vehiculo-cruzando)) (inc (nth sem 10)) (nth sem 10))
        ultimo-cruce-actualizado (if (not-empty vehiculo-cruzando) tiempo-actual (nth sem 6))
        vehiculos-pueden-pasar-actualizados(if (not-empty vehiculo-cruzando) (rest vehiculos-pueden-pasar) vehiculos-pueden-pasar)

        sem-actualizado (assoc sem
                            3 vehiculos-actualizados
                            6 ultimo-cruce-actualizado
                            7 vehiculos-pueden-pasar-actualizados
                            8 vehiculos-pasados-actualizados
                            9 tiempo-en-pasar-actualizado
                            10 tiempo-muerto-actualizado)]

    (when (and (= :verde (nth sem 5)) (empty? vehiculos-pueden-pasar) (empty? vehiculo-cruzando))
      (imprimirYRegistrarLog crucero-id "Incremento tiempo muerto en semáforo %d en tiempo %d" false (nth sem 0) tiempo-actual))
    (when (not-empty vehiculos-llegan-ahora)
      (imprimirYRegistrarLog crucero-id "Llega vehículo al semáforo %d en tiempo %d" false (nth sem 0) tiempo-actual))
    (when (not-empty vehiculo-cruzando) 
        (imprimirYRegistrarLog crucero-id "Vehículo llegado en tiempo %d cruzó semáforo %d y tardó %d segundos en hacerlo." false (first vehiculo-cruzando) (nth sem 0) (- tiempo-actual (first vehiculo-cruzando)))
        (imprimirYRegistrarLog crucero-id "Han pasado %d vehículos en el semáforo %d" false (nth sem-actualizado 8) (nth sem 0))
        (imprimirYRegistrarLog crucero-id "Contador interno tiempo en pasar: %d" false (nth sem-actualizado 9) (nth sem 0))
        )
    (when (not-empty vehiculos-pueden-pasar)
      (imprimirYRegistrarLog crucero-id "Vehículos que van a pasar el semáforo %d: %s" false (nth sem 0) (pr-str (into [] (nth sem-actualizado 7)))))

    sem-actualizado))

; Actualiza el estado y los vehiculos de un solo semaforo.
(defn actualizar-semaforo-con-vehiculos [crucero-id sem tiempo-actual tiempo-cruce]
  (procesar-vehiculos crucero-id (actualizar-estado-semaforo sem) tiempo-actual tiempo-cruce))

; Actualiza el estado y los vehiculos de los semaforos proporcionados.
(defn actualizar-semaforos-con-vehiculos [crucero-id semaforos tiempo-actual tiempo-cruce]
  (map #(actualizar-semaforo-con-vehiculos crucero-id % tiempo-actual tiempo-cruce) semaforos))

;; ============================================================
;;              Funciones para el Ciclo de Simulación
;; ============================================================
; Simulamos el tráfico del crucero
(defn ciclo-simular-trafico [crucero-id tiempo-final tiempo-cruce semaforos tiempo-actual]
  (when (< tiempo-actual tiempo-final)
    (imprimirYRegistrarLog crucero-id "\n" false)
    (imprimirYRegistrarLog crucero-id "#########################" false)
    (imprimirYRegistrarLog crucero-id "Tiempo: %d\n" false tiempo-actual)
    (reescribir-semaforos crucero-id (actualizar-semaforos-con-vehiculos crucero-id semaforos tiempo-actual tiempo-cruce))
    (imprimir-estado-semaforos crucero-id (obtener-semaforos-crucero crucero-id) tiempo-actual)
    (imprimir-vehiculos-restantes-crucero crucero-id)
    (imprimirYRegistrarLog crucero-id "#########################" false)
    (recur crucero-id tiempo-final tiempo-cruce (obtener-semaforos-crucero crucero-id) (inc tiempo-actual))))

; Iniciamos el procesamiento de un solo crucero
(defn procesar-crucero [crucero-config]
  (let [id (first crucero-config)
        semaforos (inicializar-semaforos (nth crucero-config 1))]
    (imprimirYRegistrarLog id "Procesando crucero %d con %d semáforos" true id (count semaforos))
    (reescribir-semaforos id semaforos)
    (ciclo-simular-trafico id 300 4 semaforos 0)            ;; Simula por 30 segundos, con tiempo de cruce de 4
    (imprimirYRegistrarLog id "Crucero %d simulación completada" true id)
    id))

;; ============================================================
;;                        Función Principal
;; ============================================================
; Procesa todas las particiones de cruceros
(defn procesar-particion-cruceros [cruceros]
  (doall (map (comp procesar-crucero #(leer-archivo (str "crucero_" % ".txt"))) cruceros)))

(defn -main []
  (borrar-archivos)
  (time
   (doall
    (pmap procesar-particion-cruceros
          (partition-all 5 (leer-archivo "ciudad_config.txt")))))
  (time (imprimir-estadisticas-ciudad))

  ; Ejecución de la simulación completa sin paralelismo
  (comment
    (time (procesar-particion-cruceros (leer-archivo "ciudad_config.txt")))
    (time (imprimir-estadisticas-ciudad)))
  
  (println "La simulación ha finalizado completamente"))

;; Ejecuta la función principal
(-main)