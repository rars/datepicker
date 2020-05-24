(defproject datepicker "0.1.0-SNAPSHOT"
  :description "Datepicker written in Clojurescript using Reagent."
  :url "https://github.com/rars/datepicker"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}

  :cljsbuild  {:builds [{:id "dev"
                         :source-paths ["src"]
                         :figwheel true
                         :compiler
                         {:optimizations :none
                          :output-to "resources/public/javascripts/dev.js"
                          :output-dir "resources/public/javascripts/cljs-dev"
                          :pretty-print true
                          :source-map true}}]}

  :plugins [[lein-cljsbuild "1.1.8"]
            [lein-figwheel "0.5.20"]]

  :figwheel {:css-dirs ["resources/public/stylesheets"]}

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.764"]
                 [reagent "0.10.0"]
                 [org.clojure/core.async "1.2.603"]])
