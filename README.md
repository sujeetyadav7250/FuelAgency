# these are the steps to run boooking module

**step 1 :**
Install necesaary packages in both Frontend and Backend. Run this 
```
mvn clean install
```
command in project directory. make sure there should be no previous data or table in booking database which you create in your mysql database. If you set the password for your database once check ```application.properties``` file in Backend project.

**step 2 :**
for running the Frontend and backend project from terminal use this command 
```
mvn clean spring-boot:run
```
Remember to start mysql and apache if you are using XAMPP.

**step 3 :**
create dummy data using POSTMAN in customer and cylinder table. Hit these API endpoints as mention below.
### POST http://localhost:8080/api/users
JSON
```
{
  "firstName": "Manas",
  "lastName": "gupta",
  "email": "harshguptahype@gmail.com",
  "phone": "4394568692",
  "address": "234 Main Street, Malborne",
  "connectionType": "DOMESTIC",
  "connectionStatus": "ACTIVE",
  "role": "ADMIN"
}
```
### go to http://localhost:1000/ and login as admin with userId and password which where sent on email 
### and can be viewed in postman after creating admin with json to first create customer from customer management,
### and then create supplier from supplier management and then add cylinder from cylinder management and 
### then login as a customer with customerId and Password which sent on email.
