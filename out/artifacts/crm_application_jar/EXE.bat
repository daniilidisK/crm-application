@ECHO OFF
set CLASSPATH=.
set CLASSPATH=%CLASSPATH%;crm-application.jar

java -Xnoclassgc --module-path ".\javafx-sdk-16\lib" --add-modules javafx.controls,javafx.fxml --add-exports javafx.base/com.sun.javafx.event=ALL-UNNAMED -jar crm-application.jar