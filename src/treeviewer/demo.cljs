(ns treeviewer.demo
  (:require [clojure.browser.dom :as cdom]
            [clojure.browser.event :as event]
            [cljs.reader :as reader]
            [goog.dom :as gdom]
            [treeviewer.core :as tv]))

(def text-input (cdom/get-element "text-input"))

(defn update-tree []
  (tv/clear-tree "display")
  (let [v (.value text-input)
        r (reader/read-string v)] ;; needs error handling
    (tv/render-tree "display" r)))

(defn main []
  (set! (.value text-input) "{:a 1 :b [2 3] :c #{4 5} :d {:e \"a string\"}}")
  (update-tree)
  (event/listen (cdom/get-element "treerize")
                :click
                update-tree))

(main)
