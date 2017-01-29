(ns datepicker.core
  (:require-macros [cljs.core.async.macros :refer (go)])
  (:require [reagent.core :as reagent]
            [datepicker.date :refer (calendar-view-month months-in-year get-month-name)]
            [cljs.core.async :refer (chan <! put!)]))

(def EVENTCHANNEL (chan))

(defn next-month [state]
  (let [month (:viewing-month state)
        year (:viewing-year state)
        next-month (if (== month 11)
                     0
                     (+ month 1))
        next-year (if (== month 11)
                    (+ year 1)
                    year)]
    (assoc-in (assoc-in state [:viewing-year] next-year)
      [:viewing-month] next-month)))

(defn prev-month [state]
  (let [month (:viewing-month state)
        year (:viewing-year state)
        prev-month (if (== month 0)
                     11
                     (- month 1))
        prev-year (if (== month 0)
                    (- year 1)
                    year)]
    (assoc-in (assoc-in state [:viewing-year] prev-year)
      [:viewing-month] prev-month)))

(defn next-year [state]
  (let [year (:viewing-year state)]
    (assoc-in state [:viewing-year] (+ year 1))))

(defn prev-year [state]
  (let [year (:viewing-year state)]
    (assoc-in state [:viewing-year] (- year 1))))

(defn next-decade [state]
  (let [year (:viewing-decade state)]
    (assoc-in state [:viewing-decade] (+ year 10))))

(defn prev-decade [state]
  (let [year (:viewing-decade state)]
    (assoc-in state [:viewing-decade] (- year 10))))

(defn change-view [state view]
  (assoc-in state [:current-view] view))

(defonce app-state
  (reagent/atom
   {:selected-date 30
    :selected-month 9
    :selected-year 2016
    :viewing-month 9
    :viewing-year 2016
    :viewing-decade 2010
    :current-view 'month
    :view-type 'month
    :is-hidden true}))

(def EVENTS
  {:update-state (fn [{:keys [op]}]
                   (swap! app-state (fn [state] (apply op [state]))))})

(go
  (while true
    (let [[event-name event-data] (<! EVENTCHANNEL)]
      ((event-name EVENTS) event-data))))

(defn toggle-visibility [state]
  (let [visible (:is-visible state)]
    (assoc-in state [:is-visible] (not visible))))

(defn select-date-handler [date month year]
  (fn [state] 
    (toggle-visibility (assoc-in
                        (assoc-in
                         (assoc-in state
                                   [:selected-date] date)
                         [:selected-month] month)
                        [:selected-year] year))))

(defn select-month-handler [month year]
  (fn [state]
    (let [view-type (:view-type state)]
      (case view-type
        month (change-view (assoc-in state [:viewing-month] month) 'month)
        year (assoc-in (assoc-in state [:selected-month] month) [:selected-year] year)))))

(defn select-year-handler [year]
  (fn [state]
    (case (:view-type state)
      decade (assoc-in state [:selected-year] year)
      (change-view (assoc-in state [:viewing-year] year) 'year))))

(defn create-click-handler [channel op]
  (fn [event] (put! channel [:update-state {:op op}])))

(defn create-visibility-handler [channel]
  (fn [event]
    (put! channel [:update-state {:op toggle-visibility}])))

(defn create-arrow-attrs [on-click]
  {:class "button nav-arrow"
   :type "button"
   :on-click on-click})

(defn create-header [title on-left-click on-right-click]
  [:div {:class "nav-bar"}
   [:button (create-arrow-attrs on-left-click)
    [:i {:class "fa fa-chevron-left"}]]
   title
   [:button (create-arrow-attrs on-right-click)
    [:i {:class "fa fa-chevron-right"}]]])

(defn decade-view [EVENTCHANNEL year]
  (let [selected-year (:selected-year @app-state)]
    [:div {:class "decade-view"}
     (create-header [:div {:class "nav-header"} (str year "-" (+ year 11))]
                    (create-click-handler EVENTCHANNEL prev-decade)
                    (create-click-handler EVENTCHANNEL next-decade))
     (for [years (partition 3 (range year (+ year 12)))]
       ^{:key (first years)}
       [:div {:class "decade-row"}
        (for [year years]
          ^{:key year}
          [:button {:class (if (= selected-year year) "button selected year-item" "button year-item")
                    :type "button"
                    :on-click (create-click-handler EVENTCHANNEL (select-year-handler year))}
           year])])]))

(defn year-view [EVENTCHANNEL year]
  (let [state @app-state
        is-selected-year (= (:selected-year state) year)
        selected-month (:selected-month state)]
    [:div {:class "year-view"}
     (create-header
      [:button {:class "button nav-header"
                :type "button"
                :on-click (create-click-handler EVENTCHANNEL (fn [state] (change-view state 'decade)))}
       year]
      (create-click-handler EVENTCHANNEL prev-year)
      (create-click-handler EVENTCHANNEL next-year))
     (for [month-row (partition 3 (months-in-year))]
       ^{:key (first month-row)}
       [:div {:class "month-row"}
        (for [month month-row]
          ^{:key month}
          [:button {:class (if (and is-selected-year
                                    (= (:id month) selected-month))
                             "button selected month-item" "button month-item")
                    :type "button"
                    :on-click (create-click-handler EVENTCHANNEL (select-month-handler (:id month) year))}
           (:name month)])])]))
        
(defn month-view-row [row is-selected-month]
  (let [selected-date (:selected-date @app-state)
        selected-month (:selected-month @app-state)]
    [:div {:class "row"}
     (for [date row]
       ^{:key (:date date)}
       [:button {:class (if (and is-selected-month
                              (== (:month date) selected-month)
                              (== (:date date) selected-date))
                       "button selected date-item" "button date-item")
                 :type "button"
                 :on-click (create-click-handler EVENTCHANNEL (select-date-handler (:date date) (:month date) (:year date)))}
        (:date date)])]))
               
(defn month-view [EVENTCHANNEL month year]
  (let [state @app-state
        is-selected-month (and (= (:selected-month state) month)
                               (= (:selected-year state) year))]
    [:div {:class "month-view"}
     (create-header
      [:button {:class "button nav-header"
                :type "button"
                :on-click (create-click-handler EVENTCHANNEL (fn [state] (change-view state 'year)))}
       (str (get-month-name month) " " year)]
      (create-click-handler EVENTCHANNEL prev-month)
      (create-click-handler EVENTCHANNEL next-month))
     [:div {:class "header-row"}
      (for [day ["Su" "Mo" "Tu" "We" "Th" "Fr" "Sa"]]
        ^{:key day}
        [:div {:class "day-header"}
         day])]
     (for [row (calendar-view-month month year)]
       ^{:key (apply * (map :date row))}
       [month-view-row row is-selected-month])]))

(defn datepicker-input [EVENTCHANNEL]
  (let [state @app-state]
    [:div {:class "date-select"}
     [:input {:class "date-input"
              :type "text"
              :value (str (:selected-date state)
                          "/"
                          (get-month-name (:selected-month state))
                          "/"
                          (:selected-year state))}]
     [:button {:class "date-select"
               :on-click (create-visibility-handler EVENTCHANNEL)}
      [:i {:class "fa fa-calendar"}]]]))

(defn datepicker-view [EVENTCHANNEL]
  (let [state @app-state
        current-view (:current-view state)]
    [:div {:class "datepicker-view"}
     (case current-view
       month [month-view EVENTCHANNEL (:viewing-month @app-state) (:viewing-year @app-state)]
       year [year-view EVENTCHANNEL (:viewing-year @app-state)]
       decade [decade-view EVENTCHANNEL (:viewing-decade @app-state)])]))
  
(defn datepicker []
  [:div {:class "container"}
   (let [state @app-state
         is-hidden (:is-visible state)]
     (if is-hidden
       [datepicker-input EVENTCHANNEL]
       [:div {:class "datepicker-selector"}
        [datepicker-input EVENTCHANNEL]
        [datepicker-view EVENTCHANNEL]]))])

(reagent/render [datepicker] (js/document.querySelector "#cljs-target"))
