(ns treeviewer.core
  (:require [clojure.browser.dom :as cdom]
            [clojure.browser.event :as event]
            [goog.object :as o]
            [goog.string :as s]
            [goog.ui.tree.BaseNode.EventType :as EventType]
            [goog.ui.tree.TreeControl :as TreeControl]))

(declare add-data)

(defn array? [x]
  (= "array" (goog.typeOf x)))

(defn object? [x]
  (= "object" (goog.typeOf x)))

(defn tree-control []
  (goog.ui.tree.TreeControl. "root"
                             (.defaultConfig goog.ui.tree.TreeControl)))

(defn render [component element-id]
  (.render component (cdom/get-element element-id))
  component)

(defn clear-tree [element-id]
  (cdom/remove-children element-id))

(defn render-tree [element-id data]
  (let [tree (tree-control)]
    (-> tree
        (add-data data)
        (render element-id))))

(defn create-child [node]
  (let [child (.createNode (. node (getTree)) "")]
    (.add node child)
    child))

(defn set-html [node s]
  (.setHtml node (s/htmlEscape s)))

(defn label [node x]
  (set-html node (pr-str x)))

(defn short-label [node x]
  (set-html node (str (.substring (pr-str x) 0 10) "...")))

(defn add-map [node m]
  (label node m)
  (doseq [[k v] m]
    (let [child (create-child node)]
      (label child [k v])
      (add-data (create-child child) v))))

(defn add-object [node obj]
  (label node obj)
  (let [child (create-child node)]
    (event/listen node
                goog.ui.tree.BaseNode.EventType/BEFORE_EXPAND
                #(o/forEach obj
                            (fn [v k obj]
                              (label child k)
                              (add-data (create-child child) v))))))

(defn add-seq [node l]
  (label node l)
  (doseq [item l]
    (add-data (create-child node) item)))

(defn add-seq? [x]
  (or (sequential? x)
      (set? x)
      (array? x)))

(defn add-data [node x]
  (let [add-to (cond (map? x) add-map
                     (add-seq? x) add-seq
                     (object? x) add-object
                     :otherwise label)]
    (add-to node x)
    node))


