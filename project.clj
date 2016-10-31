(defproject datepicker "0.1.0-SNAPSHOT"
  :description "Datepicker demo in clojure reagent"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :cljsbuild  {:builds [{:id "dev"
                         :source-paths ["src"]
                         :figwheel true
                         :compiler
                         {:optimizations :none
                          :output-to "resources/public/javascripts/dev.js"
                          :output-dir "resources/public/javascripts/cljs-dev"
                          :pretty-print true
                          :source-map true}}]}

  :plugins [[lein-cljsbuild "1.1.4"]
            [lein-figwheel "0.5.8"]]

  :figwheel {:css-dirs ["resources/public/stylesheets"]}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.293"]
                 [reagent "0.6.0"]
                 [org.clojure/core.async "0.2.395"]])
