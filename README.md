# Siden
tiny web application framework for Java SE 8.

Siden focus on writing your application quickly and running server more faster.

## Getting Started

### Write simple java application

```java
import ninja.siden.App;

public class Main {
	public static void main(String[] args) {
		App app = new App();
		app.get("/hello", (req, res) -> "Hello world");
		app.listen();
	}
}
```

### Add dependency to your build

```groovy
apply plugin: 'java'

repositories {
	maven {
		url "http://dl.bintray.com/taichi/maven"
	}
}

dependencies {
	compile 'ninja.siden:siden-core:0.0.1'
}

sourceCompatibility = targetCompatibility = 1.8
```

### Run and View

    http://localhost:8080/hello


# License

Apache License, Version 2.0

# Inspired projects

* http://expressjs.com/
* http://www.sinatrarb.com/
* http://www.sparkjava.com/
* http://flask.pocoo.org/

