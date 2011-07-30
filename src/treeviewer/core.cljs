(ns treeviewer.core
  (:require [goog.dom :as dom]
            [goog.ui.tree.TreeControl :as TreeControl]
            [goog.string :as s]
            [goog.object :as o]
            [goog.debug.DivConsole :as DivConsole]
            [goog.debug.LogManager :as LogManager]
            [goog.events :as events]
            [goog.events.EventType :as event-type]
            [goog.ui.tree.BaseNode.EventType :as EventType]))

(def debug-console
  (doto (goog.debug.DivConsole. (dom/getElement "div-console"))
    (.setCapturing true)))

(def logger (goog.debug.LogManager/getRoot))

(defn info [s]
  (.info logger s))

(declare add-data)

(defn array? [x]
  (= "array" (goog.typeOf x)))

(defn object? [x]
  (= "object" (goog.typeOf x)))

(defn tree-control []
  (goog.ui.tree.TreeControl. "root"
                             (.defaultConfig goog.ui.tree.TreeControl)))

(defn render-tree [element-id data]
  (-> (tree-control)
      (add-data data)
      (.render (dom/getElement element-id))))

(defn create-child [node]
  (let [child (.createNode (.getTree node ()) "")]
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
                   #(do (info "Exanding")
                        (o/forEach obj
                                   (fn [v k obj]
                                     (label child k)
                                     (add-data (create-child child) v)))
                        (info "Expanded")))))

(defn add-seq [node l]
  (label node l)
  (doseq [item l]
    (add-data (create-child node) item)))

(defn add-data [node x]
  (let [add-to (cond (map? x) add-map
                     (or (sequential? x)
                         (set? x)
                         (array? x)) add-seq
                     (object? x) add-object
                     :otherwise label)]
    (add-to node x)
    node))

(defn render-sample []
  (info "Start...")
  #_(dom/setTextContent (dom/getElement "debug") (pr-str (conj #{:a} :b)))
  (render-tree "display" {:a 1
                          :b [1 2]
                          :c {:d 1
                              :e 2
                              :f "foo"}})
  (render-tree "display"
               [1 2
                {:a 1}
                [:b :c :d]
                {:b '(1 2 3)} #{5 3 2} (array 1 2 3)
                (tree-control)])
  (info "Done."))

(render-sample)
