# Http server

## Start
   java -jar httpserver.jar <br/> 

Usage: [Options]  <br/>
Options:  <br/>
-help <arg>     Display help information.<br/>  
-p <arg>        Port. 8080 will be used if not provided.<br/>
-h <arg>        Host name. localhost will be used if not provided <br/> 
-d <arg>        Document Base(absolute or relative)  <br/>
                Current folder will be used if not provided  <br/>

## Tests
IntegrationTest.java  <br/>
RequestResponseTest.java  <br/>

## TODO  
- Thread pooling  <br/>
- Http session  <br/>
- POST, PUT and others <br/> 
- Compressed response  <br/>
- Logging  <br/>
- A lot of other stuff  <br/>