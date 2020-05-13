echo off
cd com&&echo NUL>_.class&&del /s /f /q *.class&&cd ../
cls
javac -cp ./build/;./; -d ./build/ com/krzem/mc_server_plugin/Main.java&&cd build&&jar cvf plugin.jar com/krzem/mc_server_plugin/Main.class * ../plugin.yml
copy plugin.jar "C:\Users\aleks\AppData\Roaming\Microsoft\Windows\Start Menu\Programs\mcserver\plugins\"
cd D:\K\Coding\projects\Java-Mc_Server_Plugin\