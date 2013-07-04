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

;; Commands ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti command
  (fn [server session line]
    (keyword (first (split line #" ")))))

(defmethod command :default
  [_ session _]
  (.println (:out session) "What?"))

(defmethod command :look
  [_ session _]
  ;; Slime
  (.println (:out session)
            "Your bedroom includes a bed and a small round window that gazes out into space. Canned sardines have more room than this."))

;; Server is just {:socket server-socket :players #{}}
;; Player is {:in buffered-reader :out print-reader}

(defn handle-client [csock server]
  (println "Client connected!")
  (with-open [in (io/input-stream csock)
              out (io/output-stream csock)]
    (let [session {:in (io/reader in)
                   :out (PrintWriter. out true)}]

      ;; Add player session to server's players.
      (swap! (:players server) conj session)

      ;; Greet player with server stats.
      (.println (:out session)
                (str "\nWelcome to Multima.\n"
                     "Players: " (count @(:players server))
                     "\n"))

      ;; Start player repl.
      (binding [*in* (:in session)
                *out* (:out session)]
        (loop [line (prompt "You awake in a chamber.")]
          (when-not (or (blank? line) (= line "quit"))
            (command server session line)
            (recur (prompt)))))

      ;; Remove player when done.
      (.println (:out session) "Quitting...")
      (swap! (:players server) disj session)
      )))

(defn make-server [port]
  (let [server {:socket (ServerSocket. port)
                :players (atom #{})}]
    (while true
      (with-open [csock (.accept (:socket server))]
        (handle-client csock server)))))

(defn -main [& args]
  (make-server 5000))
