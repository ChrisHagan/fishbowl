(ns net.thegeez.jetty-async-adapter
  "Adapter for the Jetty webserver, with async HTTP."
  (:import (org.eclipse.jetty.server.handler AbstractHandler)
           (org.eclipse.jetty.server Server Request Response)
           (org.eclipse.jetty.server.bio SocketConnector)
           (org.eclipse.jetty.continuation Continuation ContinuationSupport ContinuationListener)
           (org.eclipse.jetty.io EofException)
           (javax.servlet.http HttpServletRequest))
  (:require [ring.util.servlet :as servlet]))

;; Based on ring-jetty-async-adapter by Mark McGranaghan
;; (https://github.com/mmcgrana/ring/tree/jetty-async)
;; This uses Continuation instead of AsyncContext
;; has different time-out handling
;;
;; This is based on the Suspend/Resume usage
;; http://download.eclipse.org/jetty/stable-7/apidocs/org/eclipse/jetty/continuation/Continuation.html

(defn- proxy-handler
  "Returns an Jetty Handler implementation for the given Ring handler."
  [handler options]
  (proxy [AbstractHandler] []
    (handle [target ^Request base-request ^HttpServletRequest request response]
      (let [request-map (servlet/build-request-map request)
            response-map (handler request-map)]
        (condp = (:async response-map)
            nil
          (do
            (servlet/update-servlet-response response response-map)
            (.setHandled base-request true))
          :http
          (let [reactor (:reactor response-map)
                ;; continuation lives until written to!
                continuation (.startAsync request)
                _ (do (println "is expired?" (.isExpired continuation))
                      (when (.isExpired continuation)
                          (throw (Exception. "continuation is expired!"))))
                emit (fn [args]
                       (let [type (:type args)
                             servlet-response (.getServletResponse continuation)]
                         (case type
                               :head
                               (doto servlet-response
                                 (servlet/set-status (:status args))
                                 (servlet/set-headers (assoc (:headers args)
                                                        "Transfer-Encoding" "chunked"))
                                 (.flushBuffer)
                                 ;; (-> .getOutputStream .flush)
                                 )
                               :chunk
                               ;; flush will throw EofException if
                               ;; the connection is closed
                               ;; resume raises IllegalStateException
                               (try
                                 ;;(.resume continuation)
                                 (println "chunck " (:data args
                                                           ))

                                 (doto (.getWriter response)
                                   (.write (:data args))
                                   (.flush))
                                 (println "CheckError" (.checkError (.getWriter response)))
                                 (when (.checkError (.getWriter response))
                                   (throw (Exception. "CANNOT WRITE TO STREAMING CONNECTION")))
                                 
                                 #_(doto (.getOutputStream servlet-response)
                                     (.print (:data args))
                                     .flush
                                     
                                     )
                                 (catch Exception e
                                   (println "Exception was " e)
                                   (throw e)))
                               :error
                               (.sendError servlet-response (:status-code args) (:message args))
                               :close
                               (.complete continuation))))]
            (.addContinuationListener continuation
                                      (proxy [ContinuationListener] []
                                        (onComplete [c]
                                                    (println "on complete"))
                                        (onTimeout [c]
                                                   (println "on timeout")
                                                   (.complete c))))

            ;; 4 minutes is google default
            (.setTimeout continuation (get options :response-timeout (* 4 60 1000)))
            (reactor emit)
            ))))))

(defn- create-server
  "Construct a Jetty Server instance."
  [options]
  (let [connector (doto (SocketConnector.)
                    (.setPort (options :port 80))
                    (.setHost (options :host)))
        server    (doto (Server.)
                    (.addConnector connector)
                    (.setSendDateHeader true))]
    server))

(defn ^Server run-jetty-async
  "Serve the given handler according to the options.
  Options:
    :configurator   - A function called with the Server instance.
    :port
    :host
    :join?          - Block the caller: defaults to true.
    :response-timeout"
  [handler options]
  (let [^Server s (create-server (dissoc options :configurator))]
    (when-let [configurator (:configurator options)]
      (configurator s))
    (doto s
      (.setHandler (proxy-handler handler options))
      (.start))
    (when (:join? options true)
      (.join s))
    s))
