(ns datepicker.core
  (:require-macros [cljs.core.async.macros :refer (go)])
  (:require [reagent.core :as reagent]
            [datepicker.title :refer (header item-list)]
            [datepicker.date :refer (calendar-view-month months-in-year get-month-name)]
            [cljs.core.async :refer (chan <!)]))

(def EVENTCHANNEL (chan))

(defonce app-state
  (reagent/atom
   {:selected-date 30
    :selected-month 9
    :selected-year 2016
    :viewing-month 9
    :viewing-year 2016
    :viewing-decade 2010}))

(def EVENTS
  {:update-active-item (fn [{:keys [active-item]}]
                         (swap! app-state assoc-in [:active-item] active-item))})

(go
  (while true
    (let [[event-name event-data] (<! EVENTCHANNEL)]
      ((event-name EVENTS) event-data))))

(defn decade-view [year]
  (let [selected-year (:selected-year @app-state)]
    [:div {:class "decade-view"}
     (for [years (partition 3 (range year (+ year 12)))]
       ^{:key (first years)}
       [:div {:class "decade-row"}
        (for [year years]
          ^{:key year}
          [:div {:class (if (= selected-year year) "selected year-item" "year-item")}
           year])])]))

(defn year-view [year]
  (let [state @app-state
        is-selected-year (= (:selected-year state) year)
        selected-month (:selected-month state)]
    [:div {:class "year-view"}
     (for [month-row (partition 3 (months-in-year))]
       ^{:key (first month-row)}
       [:div {:class "month-row"}
        (for [month month-row]
          ^{:key month}
          [:div {:class (if (and is-selected-year
                                 (= (:id month) selected-month))
                          "selected month-item" "month-item")}
           (:name month)])])]))
        
(defn month-view-row [row is-selected-month]
  (let [selected-date (:selected-date @app-state)
        selected-month (:selected-month @app-state)]
    [:div {:class "row"}
     (for [date row]
       ^{:key (:date date)}
       [:div {:class (if (and (== (:month date) selected-month)
                              (== (:date date) selected-date))
                       "selected date-item" "date-item")}
        (:date date)])]))
               
(defn month-view [month year]
  (let [state @app-state
        is-selected-month (and (= (:viewing-month state) month)
                               (= (:viewing-year state) year))]
    [:div {:class "month-view"}
     [:div {}
      [:button {:class "nav-arrow"}
       "<"]
      [:div {:class "nav-header"}
       (get-month-name month)]
      [:button {:class "nav-arrow"}
       ">"]]
     (for [day ["Su" "Mo" "Tu" "We" "Th" "Fr" "Sa"]]
       ^{:key day}
       [:div {:class "day-header"}
        day])
     (for [row (calendar-view-month month year)]
       ^{:key (apply * (map :date row))}
       [month-view-row row is-selected-month])]))

(defn app []
  [:div {:class "container"}
   ; [header (:message @app-state)]
   ; [item-list EVENTCHANNEL (:items @app-state) (:active-item @app-state)]
   [month-view (:viewing-month @app-state) (:viewing-year @app-state)]
   [year-view (:viewing-year @app-state)]
   [decade-view (:viewing-decade @app-state)]])

(reagent/render [app] (js/document.querySelector "#cljs-target"))
