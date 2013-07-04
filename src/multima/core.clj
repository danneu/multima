(ns multima.core
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

(def chamber
  {:id :chamber
   :name "Your chamber"
   :desc "Your chamber includes a bed and a small round window that gazes out into space. Canned sardines have more room than this."})

(defn current-room [session]
  (last (:history @session)))

;; Commands ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti command
  (fn [server session line]
    (keyword (first (split line #" ")))))

(defmethod command :default
  [_ session _]
  (println "What?"))

(defmethod command :look
  [_ session _]
  ;; Slime
  (println (:desc (current-room session))))

;; Server is just {:socket server-socket :players #{}}
;; Player is {:in buffered-reader :out print-reader}

(defn make-session [in out]
  (atom {:in (io/reader in)
         :out (PrintWriter. out true)
         :history [chamber]}))

(defn handle-client [csock server]
  (println "Client connected!")
  (with-open [in (io/input-stream csock)
              out (io/output-stream csock)]
    (let [session (make-session in out)]

      ;; Add player session to server's players.
      (swap! (:players server) conj session)

      ;; Start player repl.
      (binding [*in* (:in @session)
                *out* (:out @session)]
        ;; Greet player with server stats.
        (println (str "\nWelcome to Multima.\n"
                       "Players: " (count @(:players server))
                       "\n"))

        (loop [line (prompt "You awake in a chamber.")]
          (when-not (or (blank? line) (= line "quit"))
            (command server session line)
            (recur (prompt)))))

      ;; Remove player when done.
      (.println (:out session) "Quitting...")
      (swap! (:players server) disj session))))

(defn make-server [port]
  (let [server {:socket (ServerSocket. port)
                :players (atom #{})}]
    (while true
      (with-open [csock (.accept (:socket server))]
        (handle-client csock server)))))

(defn -main [& args]
  (make-server 5000))
