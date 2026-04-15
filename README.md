# SOEN-342-Project

### Members  
|Name             | StudentID  |
| --------------- | ---------- |
|Anthony Vaccaro (team lead) | 40214876   | 
|M-Amar Kseibi    | 40276594   |
|Ariberto Bello Veras    | 40319600    |


## Deliverables

See [Iteration 4 README](iteration4/README.md) for final deliverables.

## Run Instructions

### Prerequisistes

java 21

maven (either installed system-wide or bundled with intellij)

### run with intellij

- open TaskManagerApp/pom.xml in intellij
- click on the "reload maven dependencies"
- open TaskManagerApp/src/main/java/com/soen342/Main.java
- click the run button
  
### run with maven

naivgate to ```/TaskManagerApp```

run ```mvn clean package```

run ```java -jar target/TaskManagerApp-1.0-SNAPSHOT.jar```
