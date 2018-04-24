(ns friendtest.web
   (:require [clojure.java.io :as io]
            [compojure.core :refer [defroutes GET POST ANY context]]
            [compojure.route :refer [not-found resources]]
            [ring.util.response :refer [response resource-response content-type redirect status]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.nested-params :refer [wrap-nested-params]]
            [ring.middleware.session :refer [wrap-session]]
            [clojure.java.jdbc :as j]
            [cemerick.friend :as friend]
            [cemerick.friend.workflows :refer [make-auth interactive-form]]
            [cemerick.friend.credentials :as creds]
            [hiccup.page :as h]
            [hiccup.element :as e]
            [friendtest.misc :as misc]))
           
; a dummy in-memory user "database"
(def users {"root" {:username "root"
                  :password (creds/hash-bcrypt "admin")
                  :roles #{::admin}}
           "dan"  {:username "dan"
                  :password (creds/hash-bcrypt "user")
                  :roles #{::user}}})
                  
(derive ::admin ::user)

(defn pagelinks [req]
  [:nav.navbar.navbar-dark.bg-dark.navbar-expand-lg
    [:div.container 
      [:ul.navbar-nav.mr-auto
        [:li.nav-item {:class (if (= "/" (:uri req)) "active")} [:a.nav-link {:href "/"} "Home"]]
        [:li.nav-item {:class (if (= "/user/profile" (:uri req)) "active")} [:a.nav-link {:href "/user/profile"} "My Profile"]]
        [:li.nav-item {:class (if (= "/admin/user" (:uri req)) "active")} [:a.nav-link {:href "/admin/user"} "User Admin"]]
        [:li.nav-item {:class (if (= "/admin/db" (:uri req)) "active")} [:a.nav-link {:href "/admin/db"} "DB Admin"]]
        [:li.nav-item [:a.nav-link {:href "/logout"} "Logout"]]
    ]]])

(defroutes admin-routes
  (GET "/user" req
    (h/html5
      misc/pretty-head
      [:body
        (pagelinks req)
        [:div.container "User Admin"]]))
  (GET "/db" req
    (h/html5
      misc/pretty-head
      [:body
        (pagelinks req)
        [:div.container "Database Admin"]]))
        )
    
(defroutes app-routes
  (GET "/" req
    (h/html5
      misc/pretty-head
      [:body
        (pagelinks req)
        [:div.container
          (str req)
          ]]))
  (GET "/user/profile" req
    (friend/authorize #{::user}
      (h/html5
        misc/pretty-head
        [:body
          (pagelinks req)
          [:div.container
            [:div.row (if-let [identity (friend/identity req)]
                            (apply str "Logged in, with these roles: " (-> identity friend/current-authentication :roles))
                            "anonymous user")]
            [:div.row
              (if (friend/authorized? #{::admin} (friend/identity req)) 
                [:p "Hello Admin"]
                [:p "You're Not Admin"])]
            [:div.row
              (if (friend/authorized? #{::user} (friend/identity req)) [:p "You're a user"])]
          ]])))
  (context "/admin" req
    (friend/wrap-authorize admin-routes #{::admin}))
  (GET "/login" req
    (h/html5
      misc/pretty-head
      [:body
        (pagelinks req)
        [:div.container
          [:div.card
            [:div.card-header "Login"]
            [:div.card-body
              [:form {:action "login" :method "post"}
                [:div.form-group
                  [:label {:for "username"} "Name"]
                  [:input#username.form-control {:type "text" :name "username" :placeholder "Username or email address" :auto-focus true}]]
                [:div.form-group
                  [:label {:for "password"} "Password"]
                  [:input#userpassword.form-control {:type "password" :name "password" :placeholder "Password"}]]
                [:div.form-group
                  [:label.text-danger (if (= "Y" (-> req :params :login_failed)) (str "Login failed for username " (-> req :params :username)))]
                  [:button.btn.btn-warning.float-right {:type "submit"} "Login"]]]]]
          ]]))
  (friend/logout
    (ANY "/logout" [] (redirect "/")))
  (resources "/"))
  
  
(def app 
  (-> app-routes
    (friend/authenticate 
      {:allow-anon? true
       :login-uri "/login"
       :default-landing-uri "/"
       :unauthorized-handler #(-> (h/html5 misc/pretty-head [:body (pagelinks %) [:div.container [:h3 "Access Denied: " (:uri %)]]])
                                  response
                                  (status 401))
       ;; :credential-fn #(creds/bcrypt-credential-fn users %)
       :credential-fn (partial creds/bcrypt-credential-fn users)
       :workflows [(interactive-form)]})
    (wrap-keyword-params)
    (wrap-params)
    (wrap-session)
    ))
   