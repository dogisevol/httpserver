# Http server

## Description
    It is a simple file browsing http server. Includes the following:   
    - Http server class   
    - Request and response interfaces as well as their default implementation   
    - Simple servlet class   
    - Main application that runs server   

## Start server

### Using java    
   java -jar httpserver.jar   

    Usage: [Options]    
    Options:    
    -help <arg>     Display help information.  W
    -p <arg>        Port. 8080 will be used if not provided.  
    -h <arg>        Host name. localhost will be used if not provided   
    -d <arg>        Document Base(absolute or relative). Current folder will be used if not provided    


### Using maven    
    mvn package    
    mvn exec:java -Dexec.args="arg1 arg2 ..."     

## Runing tests    
     mvn test    


## TODO  
- Thread pooling    
- Http session    
- POST, PUT and others    
- Compressed response    
- Logging    
- A lot of other stuff    