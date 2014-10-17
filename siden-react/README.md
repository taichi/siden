# React.js Server Side Rendering Support

## Installation

### get console-polyfill

* [console-polyfill](https://github.com/paulmillr/console-polyfill) 

Because Nashorn don't contain `console` object.

### Setup your javascript build environment

like below
```
npm install -g react-tools
jsx --watch assets/src build
```

siden-react don't build jsx.

### Pick your favorite JSON Serializer and Template Engine

you may use any JSON Serializer such as

* [Jackson](http://jackson.codehaus.org/)
* [google-gson](https://code.google.com/p/google-gson/)
* [boon](https://github.com/boonproject/boon)

i recommend you to use [boon](https://github.com/boonproject/boon).

you may use any template engine such as

* [Thymeleaf](http://www.thymeleaf.org/)
* [handlebars.java](https://github.com/jknack/handlebars.java)
* [mustache.java](https://github.com/spullara/mustache.java)

i recommend you to use [mustache.java](https://github.com/spullara/mustache.java).

### Add dependency to your build.gradle

```groovy
apply plugin: 'java'

repositories.jcenter()

dependencies {
	compile 'ninja.siden:siden-react:0.0.1'
}

sourceCompatibility = targetCompatibility = 1.8
```

### Example

```java
package example;

import java.nio.file.Paths;
import java.util.Arrays;

import ninja.siden.App;
import ninja.siden.react.React;

public class UseReactSSR {

	public static void main(String[] args) {
		// setup react server side rendering
		React rc = new React("HelloMessage", "content", Arrays.asList(
				// https://github.com/paulmillr/console-polyfill
				// Nashorn don't contain console object.
				Paths.get("assets", "console-polyfill.js"),
				// https://github.com/facebook/react
				Paths.get("assets", "react.js"),
				// npm install -g react-tools
				// jsx -x jsx assets build
				// siden-react don't support jsx compile.
				Paths.get("build", "hello.js")));

		App app = new App();
		app.get("/", (q, s) -> {
				// serialized json
				String props = "{\"name\":\"john\"}";
				// server side rendering
				return "<html><body>" + rc.toHtml(props) + "</body></html>";
			}).type("text/html");
		app.listen();
	}
}
```

```javascript
/** @jsx React.DOM */
var HelloMessage = React.createClass({
  render: function() {
    return <div>Hello {this.props.name}</div>;
  }
});
```

if you want to more complex example, see [here](https://github.com/taichi/siden/tree/master/siden-example/src/main/java/example/UseReactComplexSSR.java)

## Similar projects
* [React.NET](https://github.com/reactjs/React.NET)
* [Om Server Rendering](https://github.com/pleasetrythisathome/om-server-rendering)
* [jreact](https://github.com/KnisterPeter/jreact/)
