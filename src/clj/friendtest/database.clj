(ns friendtest.database
  (:require 
    [clojure.java.jdbc :as j]
    [cemerick.friend [credentials :as creds]]))
      
;; Database for saving and loading decks     
;;  (def db (or (System/getenv "DATABASE_URL")
;;              {:classname   "org.sqlite.JDBC"
;;               :subprotocol "sqlite"
;;               :subname     "resources/db/conquestdb.sqlite3"
;;               }))
;;  ;;     
;;  (defn- create-db []
;;     (try (j/db-do-commands db
;;              (j/create-table-ddl :decks
;;                 [[:name :text]
;;                  [:author :int]
;;                  [:data :blob]]))
;;          (catch Exception e ())))
;;  ;; Migrate
;;             
;;  (defn update-or-insert!
;;    "Updates columns or inserts a new row in the specified table"
;;    [db table row where-clause]
;;    (j/with-db-transaction [t-con db]
;;      (let [result (j/update! t-con table row where-clause)]
;;        (if (zero? (first result))
;;          (j/insert! t-con table row)
;;          result))))
;;          
;;  (defn- save-data [name deck]
;;     (create-db)
;;     (update-or-insert! db :decks {:data deck :author 0 :name name} ["name = ?" name]))