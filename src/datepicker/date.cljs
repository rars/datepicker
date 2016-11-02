(ns datepicker.date)

(defn get-month-name [month]
  (get ["January" "February" "March" "April" "May" "June" "July" "August" "September" "October" "November" "December"] month))

(defn months-in-year []
  (map (fn [i] {:id i :name (get-month-name i)}) (range 0 12)))

(defn num-days-in-month [month year]
  (if (= month 11)
    31
    (.getDate (js/Date. year (+ month 1) 0))))

(defn num-days-in-prev-month [month year]
  (if (= month 0)
    31
    (num-days-in-month (- month 1) year)))

(defn calendar-view-month [month year]
  (let [starting-day (.getDay (js/Date. year month 1))
        days-in-month (range 1 (+ 1 (num-days-in-month month year)))
        ending-day (.getDay (js/Date. year month (count days-in-month)))
        prev-month-ending-date (num-days-in-prev-month month year)
        prev-ending-days (range (- (+ 1 prev-month-ending-date) starting-day) (+ 1 prev-month-ending-date))
        ending-days (take (- 6 ending-day) (range 1 7))
        mark-month (fn [month date] {:year year :month month :date date})
        prev-month (if (== month 0) 11 (- month 1))
        next-month (if (== month 11) 0 (+ month 1))]
    (partition 7 (concat
                  (map (fn [d] (mark-month prev-month d)) prev-ending-days)
                  (map (fn [d] (mark-month month d)) days-in-month)
                  (map (fn [d] (mark-month next-month d)) ending-days)))))
    
