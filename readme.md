#IAPExample

Google Identity aware proxy for protecting GKE rest apis using service accounts.

Contains 2 modules client and server. 

## Run server
- cd server
- mvn spring-boot:run
- this will start the http server on port 8080

## Run client
- cd client
- copy the service account file at client/src/main/resources
- mvn spring-boot:run
- this will send a hello world request to server at port 8080 with bearer token