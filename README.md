# SOEN-342-Project

### Members  
|Name             | StudentID  |
| --------------- | ---------- |
|Anthony Vaccaro (team lead) | 40214876   | 
|M-Amar Kseibi    | 40276594   |
|Ariberto Bello Veras    | 40319600    |

## *Note for graders*

A merge conflict resolution caused the main branch history to be temporarily broken <br/>
It was fixed by resetting history and replaying the commits one by one. <br/>
this caused all commits after commit #[4faca8f](../../commit/4faca8f) to appear to be made by the same person even though they are not. <br/>
This means the github insights doesnt accurately count the contributions of each member to main. <br/>
The original state has been preserved in the [broken-main-copy](../../tree/broken-main-copy) branch for reference. <br/>
To see the original authors of the commits passed commit #[4faca8f](../../commit/4faca8f), refer to that branch 


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
