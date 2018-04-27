(ns friendtest.users
  (:require 
    [clojure.java.jdbc :as j]
    [cemerick.friend [credentials :as creds]]))
      
; a dummy in-memory user "database"
; (def users (atom {"root" {:username "root"
;                           :password (creds/hash-bcrypt "admin")
;                           :roles #{::admin}}
;                   "dan"  {:username "dan"
;                           :password (creds/hash-bcrypt "user")
;                           :roles #{::user}}}))

(derive ::admin ::user)

(def db (or (System/getenv "DATABASE_URL")
            {:classname   "org.sqlite.JDBC"
             :subprotocol "sqlite"
             :subname     "resources/private/db/db.sqlite3"
             }))

(defn update-or-insert!
    "Updates columns or inserts a new row in the specified table"
    [db table row where-clause]
    (j/with-db-transaction [t-con db]
      (let [result (j/update! t-con table row where-clause)]
        (if (zero? (first result))
          (j/insert! t-con table row)
          result))))
          
(defn adduser [username password admin?]
  (try (update-or-insert!
          db
          :users
          {:username username 
           :password (creds/hash-bcrypt password) 
           :admin admin?}
          ["username = ?" username])
        (catch Exception e (prn (str "Add user failed: " e)))))  
(defn updateuser [username admin?]
  (j/update! db :users {:admin admin?}
           ["username = ?" username]))
(defn dropuser [username]
  (j/delete! db :users ["username = ?" username]))
        
;; https://devcenter.heroku.com/articles/clojure-web-application
(defn migrate []
;;  (when (not (migrated?))
    (print "Creating database structure...") (flush)
    (try (j/db-do-commands db
            (j/create-table-ddl :users
              [[:username :text :primary :key]
               [:password :text]
               [:admin :boolean]]))
         (catch Exception e (prn (str "Migration failed: " e))))
    
    (adduser "root" "admin" true)
    (adduser "dan" "user" false)
    (println " done"))
  
(defn dbusers []
  (->> (j/query db ["select * from users"])
      (map (fn [x]
          {(:username x) (-> x 
                            (dissoc :admin)
                            (assoc :roles (if (:admin x) #{::admin} #{::user})))}))
      (into {})))
