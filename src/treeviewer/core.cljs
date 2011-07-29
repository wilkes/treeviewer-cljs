(ns treeviewer.core
  (:require [goog.dom :as dom]
            [goog.ui.tree.TreeControl :as TreeControl]))

(def data {:a 1 :b [1 2] :c {:d 1 :e 2 :f "foo"}})

(declare add-data)

(defn tree-control []
  (goog.ui.tree.TreeControl.
   "root"
   (.defaultConfig goog.ui.tree.TreeControl)))

(defn render [object element-id]
  (.render object (dom/getElement element-id)))

(defn create-child [node]
  (let [child (.createNode (.getTree node ()) "")]
    (.add node child)
    child))

(defn label [node x]
  (.setHtml node (pr-str x)))

(defn add-map [node m]
  (label node m)
  (doseq [[k v] m]
    (let [child (create-child node)]
      (label child k)
      (add-data (create-child child) v))))

(defn add-seq [node l]
  (label node l)
  (doseq [item l]
    (add-data (create-child node) item)))

(defn add-data [node x]
  (let [add-to (cond (map? x) add-map
                     (sequential? x) add-seq
                     :otherwise label)]
    (add-to node x)
    node))

(-> (tree-control)
    (add-data data)
    (render "display"))

