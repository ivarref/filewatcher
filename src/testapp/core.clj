(ns testapp.core
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [clojure.string :as str]
            [clojure.java.shell :as shell])
  (:gen-class)
  (:import (java.io File)))

(defn relative-pathname
  [^File file]
  (let [cwd-path (-> (io/file ".")
                     (.toPath))
        relative-path (.relativize cwd-path (.toPath file))]
    (.toString relative-path)))

(defn most-recently-changed-file
  []
  (let [files (->> (io/file ".")
                   (file-seq)
                   (remove #(.isDirectory %))
                   (remove #(.startsWith (.getName %) "."))
                   (filter #(.endsWith (.getName %) ".tex"))
                   (sort-by #(.lastModified %)))]
    (if (empty? files)
      []
      [(relative-pathname (last files)) (.lastModified (last files))])))

(defn exec-cmds
  [args filename]
  (let [cmd (conj args filename)]
    (println "executing" cmd "...")
    (let [{out :out exit :exit :err :err} (apply shell/sh cmd)]
      (println out)
      (println err)
      (println "executing" cmd "..."
               (if (= 0 exit) "OK" (str "ERROR, exit code: " exit))))))

(defn watch-files
  ([args] (watch-files args (most-recently-changed-file)))
  ([args old-file]
   (let [new-file (most-recently-changed-file)
         file-changed (and (not= old-file new-file) (not-empty new-file))]
     (when file-changed
       (exec-cmds args (first new-file)))
     (Thread/sleep 60)
     (recur args (if file-changed (most-recently-changed-file) new-file)))))

(defn -main
  [& args]
  (println "working directory is" (.getAbsolutePath (io/file ".")))
  (println "most recent file is" (most-recently-changed-file))
  (println "starting to watch files ...")
  (when (empty? args)
    (println "Please specify command to execute...!")
    (System/exit 1))
  (watch-files (vec args)))
