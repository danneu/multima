(ns multima.core
  (:require [clojure.java.io :as io])
  (:import [java.net ServerSocket]
           [java.io PrintWriter]))

(defn prompt [msg]
  (print (str msg "> ")) (flush) (read-line))

;; Server is just {:socket server-socket :players #{}}
;; Player is {:in buffered-reader :out :print-reader}

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
                (str "Welcome to Multima.\n"
                     "Players: " (count @(:players server))))

      ;; Start player repl.
      ;; ...

      ;; Remove player when done.
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
