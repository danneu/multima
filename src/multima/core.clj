(ns multima.core
  (:use [multima world])
  (:require [clojure.java.io :as io]
            [clojure.string :refer [blank? split]])
  (:import [java.net ServerSocket]
           [java.io PrintWriter]))

(defn prompt
  ([] (prompt ""))
  ([msg]
     (print (str msg "\n> "))
     (flush)
     (read-line)))

;; World ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(load-realm "ship")

(defn current-room [session]
  @(resolve (last (:history @session))))

;; Commands ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti command
  (fn [server session line]
    (keyword (first (split line #" ")))))

(defmethod command :default
  [_ session _]
  (println "What?"))

(defmethod command :look
  [_ session _]
  (describe-room (current-room session)))

(defmethod command :go
  [server session line]
  (let [exit (keyword (second (split line #" ")))
        next-room (get (:exits (current-room session)) exit)]
    (println "Heading" exit)
    (swap! session
           assoc
           :history
           (conj (:history @session) next-room))
    (describe-room (current-room session))))

;; Server ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Server is just {:socket server-socket :players #{}}
;; Player is {:in buffered-reader :out print-reader
;;            :history [<rooms>]}

(defn make-session [in out]
  (atom {:in (io/reader in)
         :out (PrintWriter. out true)
         :history ['ship/chamber]}))

(defn start-repl [server session]
  (binding [*in* (:in @session)
            *out* (:out @session)]

    (println (str "\nWelcome to Multima.\n"
                  "Players: " (count @(:players server))
                  "\n"))

    (loop [line (prompt "You awake in a chamber.")]
      (when-not (or (blank? line) (= line "quit"))
        (command server session line)
        (recur (prompt))))

    (println "Quitting...")))

(defn handle-client [csock server]
  (println "Client connected!")
  (with-open [in (io/input-stream csock)
              out (io/output-stream csock)]
    (let [session (make-session in out)]

      ;; Add player session to server's players.
      (swap! (:players server) conj session)

      ;; Start player repl.
      (start-repl server session)

      ;; Remove player when done.
      (swap! (:players server) disj session))))

(defn make-server [port]
  (let [server {:socket (ServerSocket. port)
                :players (atom #{})}]
    (while true
      (with-open [csock (.accept (:socket server))]
        (handle-client csock server)))))

(defn -main [& args]
  (make-server 5000))
