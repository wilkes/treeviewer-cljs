(ns treeviewer.core
  (:require [goog.dom :as dom]
            [goog.ui.tree.TreeControl :as TreeControl]
            [goog.string :as s]
            [goog.object :as o]
            [goog.debug.DivConsole :as DivConsole]
            [goog.debug.LogManager :as LogManager]
            [goog.events :as events]
            [goog.ui.tree.BaseNode.EventType :as EventType]
            [cljs.reader :as reader]))

(def debug-console
  (doto (goog.debug.DivConsole. (dom/$ "div-console"))
    (.setCapturing true)))

(def logger (goog.debug.LogManager/getRoot))

(defn info [& s]
  (.info logger (apply str s)))

(declare add-data)

(defn array? [x]
  (= "array" (goog.typeOf x)))

(defn object? [x]
  (= "object" (goog.typeOf x)))

(defn tree-control []
  (goog.ui.tree.TreeControl. "root"
                             (.defaultConfig goog.ui.tree.TreeControl)))

(defn render [component element-id]
  (.render component (dom/$ element-id))
  component)

(defn clear-tree [element-id]
  (dom/removeChildren (dom/$ element-id)))

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
    (events/listen node
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
  (info "adding " x)
  (let [add-to (cond (map? x) add-map
                     (add-seq? x) add-seq
                     (object? x) add-object
                     :otherwise label)]
    (add-to node x)
    node))

(defn do-button-clicked []
  (clear-tree "display")
  (let [v (.value (dom/$ "text-input"))
        r (reader/read-string v)] ;; needs error handling
    (info v)
    (render-tree "display" r)))


(defn render-sample []
  (render-tree "display"
               {:a 1
                :b [1 2 3]
                :c #{4 5 6}}))

(defn main []
  (events/listen (dom/$ "treerize")
                 "click"
                 do-button-clicked))

(main)
