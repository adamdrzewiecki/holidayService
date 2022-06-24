# Holiday Information Service

### Documentation
To run this application call in project root folder **( .\holidayService\ )**
#### Windows
.\gradlew run -D org.gradle.java.home="JAVA_HOME path" 
#### Mac/Linux
gradlew run -D org.gradle.java.home='JAVA_HOME path'

After starting application use
http://localhost:8080/swagger-ui/#/holiday-information-controller/getNextHolidayForCountriesUsingGET
to get information about next holiday after the given date that will happen on the same day in both countries.
***
Test user credentials:  
###username: swagger-ui
###password: swagger-ui
