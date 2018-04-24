(ns friendtest.misc
  (:require 
    [hiccup.page :as h]
    [cemerick.friend :as friend]))

(def pretty-head
  [:head
  ;; Meta Tags
    [:meta {:charset "UTF-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
  ;; jquery and popper
    [:script {:src "https://code.jquery.com/jquery-3.3.1.min.js" :integrity "sha256-FgpCb/KJQlLNfOu91ta32o/NMZxltwRo8QtmkMRdAu8=" :crossorigin "anonymous"}]
  ;; Bootstrap  
    [:link   {:rel "stylesheet" :href "https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" :integrity "sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" :crossorigin "anonymous"}]
    [:script {:src "https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js" :integrity "sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl" :crossorigin "anonymous"}]
  ;; Font Awesome
    [:script {:defer true :src "https://use.fontawesome.com/releases/v5.0.10/js/all.js"}]
  ;; JQuery Qtip2
    [:link   {:rel "stylesheet" :href "https://cdnjs.cloudflare.com/ajax/libs/qtip2/2.1.1/jquery.qtip.css"}]
    [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/qtip2/2.1.1/jquery.qtip.js"}]
  ;; TAFFY JQuery database
    [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/taffydb/2.7.2/taffy-min.js"}]])

(defn navbar [req]
  [:nav.navbar.navbar-expand-lg.navbar-dark {:style "background-color: teal;"}
    [:div.container
      ;; Home Brand with Icon
      [:a.navbar-brand.mb-0.h1 {:href "/"}
        [:i.fas.fa-cog.mx-2] 
        "Home"]
      ;; Collapse Button for smaller viewports
      [:button.navbar-toggler {:type "button" :data-toggle "collapse" :data-target "#navbarSupportedContent" 
                            :aria-controls "navbarSupportedContent" :aria-label "Toggle Navigation" :aria-expanded "false"}
        [:span.navbar-toggler-icon]]
      ;; Collapsable Content
      [:div#navbarSupportedContent.collapse.navbar-collapse
        ;; List of Links
        [:ul.navbar-nav.mr-auto
          [:li.nav-item 
            [:a.nav-link {:href "#"} "Decks"]]
          [:li.nav-item 
            [:a.nav-link {:href "#"} "Collection"]]
          [:li.nav-item 
            [:a.nav-link.disabled "Litmus"]]] ;; {:href "/litmus"}
        ;; Inline Search Form
          [:form.form-inline.my-2.my-lg-0
            [:div.input-group
              [:input.form-control {:type "search" :placeholder "search" :name "q" :aria-label "Search"}]
              [:div.input-group-append
                [:button.btn {:type "submit"}
                  [:i.fas.fa-search]]]]]
        ;; Login Icon
          [:span.nav-item.dropdown
            [:a#userDropdown.nav-link.dropdown-toggle.text-white {:href="#" :role "button" :data-toggle "dropdown" :aria-haspopup "true" :aria-expanded "false"}
              [:i.fas.fa-user]]
              (if-let [identity (friend/identity req)]
                [:div.dropdown-menu {:aria-labelledby "userDropdown"}
                  [:a.dropdown-item {:href "/admin"} (str identity)]
                  [:div.dropdown-divider]
                  [:a.dropdown-item {:href "/logout"} "Logout"]]                
                [:div.dropdown-menu {:aria-labelledby "userDropdown"}
                  [:a.dropdown-item {:href "/login"} "Login"]])]]]])