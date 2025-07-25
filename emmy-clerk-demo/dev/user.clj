(ns user
  (:require [mentat.clerk-utils.build :as b]
            [nextjournal.clerk :as clerk]))

(def index
  "notebooks/emmy_clerk_demo/full_demo.clj")

(def defaults
  {;; NOTE by default, your project's first notebook is registered as its index;
   ;; this means that static builds will populate `index.html` with this
   ;; notebook.
   ;;
   ;; Comment out the following line to tell Clerk to generate its own index
   ;; with a list of all built pages.
   :index index

   ;; Uncomment the following line to use the custom ClojureScript code and Emmy
   ;; namespaces loaded by `dev/emmy_clerk_demo/sci_extensions.cljs`.

   :cljs-namespaces '[emmy-clerk-demo.sci-extensions]
   })

(def serve-defaults
  (assoc defaults
         :port 7777
         :watch-paths ["notebooks"]
         :browse? true))

(def static-defaults
  (assoc defaults
         :browse? false
         :paths ["notebooks/**.clj"]
         :cname ""
         :git/url "https://github.com/emmy-clerk-demo"))

(defn serve!
  "Alias of [[mentat.clerk-utils.build/serve!]] with [[defaults]] supplied as
  default arguments.

  Any supplied `opts` overrides the defaults."
  ([] (serve! {}))
  ([opts]
   (b/serve!
    (merge serve-defaults opts))))

(def ^{:doc "Alias for [[mentat.clerk-utils.build/halt!]]."}
  halt!
  b/halt!)

(defn build!
  "Alias of [[mentat.clerk-utils.build/build!]] with [[static-defaults]] supplied
  as default arguments.

  Any supplied `opts` overrides the defaults."
  [opts]
  (b/build!
   (merge static-defaults opts)))
