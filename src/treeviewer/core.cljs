(ns treeviewer.core
  (:require [goog.dom :as dom]
            [goog.ui.tree.TreeControl :as TreeControl]
            [goog.string :as s]
            [goog.object :as o]))

(declare add-data)

(def data {:a 1 :b [1 2] :c {:d 1 :e 2 :f "foo"}})

(defn array? [x]
  (= "array" (goog.typeOf x)))

(defn object? [x]
  (= "object" (goog.typeOf x)))

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
  (o/forEach obj
             (fn [v k obj]
               (let [child (create-child node)]
                 (label child k)
                 (label (create-child child) v)))))

(defn add-seq [node l]
  (label node l)
  (doseq [item l]
    (add-data (create-child node) item)))

(defn add-data [node x]
  (let [add-to
        (cond (map? x) add-map
              (or (sequential? x)
                  (set? x)
                  (array? x)) add-seq
              (object? x) add-object
              :otherwise label)]
    (add-to node x)
    node))

(defn render-sample []
  #_(dom/setTextContent (dom/getElement "debug") (pr-str (conj #{:a} :b)))
  (-> (tree-control)
      (add-data data)
      (render "display"))
  (-> (tree-control)
      (add-data [1 2
                 {:a 1}
                 [:b :c :d]
                 {:b '(1 2 3)} #{5 3 2} (array 1 2 3)
                 (tree-control)]
                )
      (render "display")))

(render-sample)
