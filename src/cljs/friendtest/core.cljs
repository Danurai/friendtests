(ns friendtest.core
  (:require [reagent.core :as r]
           [reagent.session :as session]))
           
(defonce app-data (atom {}))