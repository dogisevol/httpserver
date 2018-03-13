# Http server

## Start server

- Using java
   java -jar httpserver.jar <br/>

    Usage: [Options]  <br/>
    Options:  <br/>
    -help <arg>     Display help information.<br/>W
    -p <arg>        Port. 8080 will be used if not provided.<br/>
    -h <arg>        Host name. localhost will be used if not provided <br/>
    -d <arg>        Document Base(absolute or relative). Current folder will be used if not provided  <br/>


- Using maven
    mvn package  <br/>
    mvn exec:java -Dexec.args="arg1 arg2 ..."   <br/>

## Runing tests
     mvn test  <br/>


## TODO  
- Thread pooling  <br/>
- Http session  <br/>
- POST, PUT and others <br/> 
- Compressed response  <br/>
- Logging  <br/>
- A lot of other stuff  <br/>