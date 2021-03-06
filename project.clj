(defproject clj-browserchannel-demo "0.0.1"
  :description "BrowserChannel"
  :url ""
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [ring/ring-core "1.1.0-SNAPSHOT" :exclusions [javax.servlet/servlet-api]]
                 [ring/ring-servlet "1.1.0-SNAPSHOT" :exclusions [javax.servlet/servlet-api]]
                 [org.eclipse.jetty/jetty-server "8.1.2.v20120308"];; includes ssl
                 [org.clojure/data.json "0.1.3"]
                 [org.clojure/clojurescript "0.0-1011" :exclusions [org.clojure/google-closure-library]]
                 [net.thegeez/google-closure-library "0.0-1698"]]
  )
