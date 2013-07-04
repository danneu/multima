(ns multima.world)

(defmacro realm [realm-name]
  `(ns ~realm-name
     (:use [multima.world])))

(defmacro room [id room-name desc & exits]
  `(def ~id
     {:id (keyword ~id)
      :name ~room-name
      :desc ~desc
      :exits (apply hash-map (quote ~exits))}))

(defn load-realm [realm-name]
  (println "Loading" realm-name)
  (load-file (str "data/" realm-name ".clj")))

(defn describe-room [room]
  (println (:desc room))
  (doseq [exit (:exits room)]
    (println (str " - "(name (key exit)) ": " (name (val exit))))))

