(ns friendtest.web
   (:require [clojure.java.io :as io]
             [clojure.data.json :as json]
             [compojure.core :refer [defroutes GET POST ANY context]]
             [compojure.route :refer [not-found resources]]
             [ring.util.response :refer [response resource-response content-type redirect status]]
             [ring.middleware.params :refer [wrap-params]]
             [ring.middleware.keyword-params :refer [wrap-keyword-params]]
             [ring.middleware.nested-params :refer [wrap-nested-params]]
             [ring.middleware.session :refer [wrap-session]]
             [cemerick.friend :as friend]
               (cemerick.friend [workflows :as workflows]
                                [credentials :as creds])
             [hiccup.page :as h]
             [hiccup.element :as e]
             [friendtest.misc :as misc]
             [friendtest.users :as users :refer [dbusers adduser dropuser updateuser]]))
            
(def cards (json/read-str (slurp (io/resource "private/data/wh40k_cards.min.json")) :key-fn keyword))

            
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
        [:div.container.my-2
          [:div.list-group
            (for [user (vals (dbusers))]
              [:div.list-group-item
                [:form.form-inline.justify-content-between {:method "post" :action "/admin/updateuser"}
                  [:div.form-group
                    [:label.h4.mr-2 "Edit User:"]
                    [:input.form-control.mr-2 {:type="text" :name "username" :hidden true :value (:username user)}]
                    [:input.form-control.disabled.mr-2 {:type="text" :disabled true :value (:username user)}]
                    [:div.form-check.mr-2
                      [:input.form-check-input {:type "checkbox" :name "admin" :checked (= (:roles user) #{::users/admin})}]
                      [:label.form-check-label "Admin"]]]
                  [:div.form-group
                    [:button.btn.btn-warning.mr-2 {:type "submit" :name "action" :value "update"} "Update"]
                    [:button.btn.btn-danger.mr-2 {:type "submit" :name "action" :value "delete"} "Delete"]]]])
            
            [:div.list-group-item
              [:form.form-inline.justify-content-between {:method "post" :action "/admin/adduser"}
                [:div.form-group
                  [:label.h4.mr-2 "New User:"]
                  [:input.form-control.mr-2 {:type "text" :placeholder "UserName" :name "username" :required true}]
                  [:input.form-control.mr-2 {:type "password" :placeholder "Password" :name "password" :required true}]
                  [:input.form-control.mr-2 {:type "password" :placeholder "Confirm Password" :name "confirm" :required true}]
                  [:div.form-check.mr-2
                    [:input.form-check-input {:type "checkbox" :name "admin"}]
                    [:label.form-check-label "Admin"]]]
                [:button.btn.btn-primary {:type "submit"} "Add User"]]]
          ]]]))
  (POST "/adduser" [username password admin]
    (adduser username password (= admin "on"))
    (redirect "/admin/user"))
  (POST "/updateuser" [username admin action]
    (if (not= username "root")
      (if (= action "delete") 
        (dropuser username)
        (updateuser username (= admin "on"))))
    (redirect "/admin/user"))
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
          [:div.h3 "Current Request"]
          [:div (str req)]
          ]]))
  (GET "/user/profile" req
    (friend/authorize #{::users/user}
      (h/html5
        misc/pretty-head
        [:body
          (pagelinks req)
          [:div.container
            [:div.row (if-let [identity (friend/identity req)]
                            (apply str "Logged in, with these roles: " (-> identity friend/current-authentication :roles))
                            "anonymous user")]
            [:div.row
              (if (friend/authorized? #{::users/admin} (friend/identity req)) 
                [:p "Hello Admin"]
                [:p "You're Not Admin"])]
            [:div.row
              (if (friend/authorized? #{::users/user} (friend/identity req)) [:p "You're a user"])]
          ]])))
  (context "/admin" req
    (friend/wrap-authorize admin-routes #{::users/admin}))
    ;admin-routes)
  (context "/api/data" []
    (GET "/cards" [] 
      (content-type (response (slurp (io/resource "private/data/wh40k_cards.min.json"))) "application/json")))
  (GET "/login" req
    (h/html5
      misc/pretty-head
      [:body
        (pagelinks req)
        [:div.container.my-2
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
                ;;[:div.form-check
                ;;  [:input#rememberme.form-check-input {:type "checkbox"}]
                ;;  [:label.form-check-label {:for "rememberme"} "Remember me."]]
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
       ;; :credential-fn #(creds/bcrypt-credential-fn @users %)
       :credential-fn #(creds/bcrypt-credential-fn (dbusers) %)
       :workflows [(workflows/interactive-form)]})
    (wrap-keyword-params)
    (wrap-params)
    (wrap-session)))
   