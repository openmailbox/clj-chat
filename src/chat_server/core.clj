(ns chat-server.core
  (:gen-class)
  (:import (java.net ServerSocket)
           (java.io PrintWriter InputStreamReader BufferedReader)))

(def port 2300)
(def users (atom []))
(declare new-user announce)

(defn run-server 
  "Main server loop to be run in a separate thread."
  []
  (let [server (ServerSocket. port)]
    (loop [socket (.accept server)]
      (let [out (PrintWriter. (.getOutputStream socket) true)
            in (BufferedReader. (InputStreamReader. (.getInputStream socket)))]
        (println "Client connected." (inc (count @users)) "total clients.")
        (.start (Thread. #(new-user in out)))
        (recur (.accept server))))))

(defn new-user
  "Handle a newly connected user."
  [in out]
  (let [user {:out out, :in in}
        user-name (inc (count @users))]
    (announce (str "User " user-name " connected.\n> "))
    (swap! users conj user)
    (.print out "Welcome!\n\n> ")
    (.flush out))
  (loop [string (.readLine in)]
    (announce (str "User " (inc (.indexOf @users {:out out, :in in})) ": " string "\n> "))
    (recur (.readLine in))))
    
(defn announce
  "Send a string to all connections."
  [string]
  (doseq [user @users]
    (let [out (get user :out)]
      (.print out (str string))
      (.flush out))))

(defn -main
  "A simple telnet based chat server."
  [& args]
  ;; work around dangerous default behaviour in Clojure
  (alter-var-root #'*read-eval* (constantly false))
  (.start (Thread. #(run-server)))
  (println "Waiting for connections on port" port))
