(ns chat-server.core
  (:gen-class)
  (:import (java.net ServerSocket)
           (java.io PrintWriter InputStreamReader BufferedReader)))

(def port 2300)
(def users (atom []))
(declare new-user)

(defn run-server 
  "Main server loop to be run in a separate thread."
  []
  (let [server (ServerSocket. port)]
    (loop [socket (.accept server)]
      (let [out (PrintWriter. (.getOutputStream socket) true)
            in (BufferedReader. (InputStreamReader. (.getInputStream socket)))]
        (swap! users conj {:out out, :in in})
        (println "Client connected." (count @users) "total clients.")
        (.start (Thread. #(new-user in out)))
        (recur (.accept server))))))

(defn new-user
  "Handle a newly connected user."
  [in out]
  (.print out "Welcome!\n\n> ")
  (.flush out)
  (loop [string (.readLine in)]
    (.print out "> ")
    (doseq [user @users]
      (let [out (get user :out)]
        (.print out string)
        (.flush out)))
    (recur (.readLine in))))
    
(defn -main
  "A simple telnet based chat server."
  [& args]
  ;; work around dangerous default behaviour in Clojure
  (alter-var-root #'*read-eval* (constantly false))
  (.start (Thread. #(run-server)))
  (println "Waiting for connections on port" port))
