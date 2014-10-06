
# React.js serverside rendering support
https://github.com/facebook/react

http://facebook.github.io/react/docs/top-level-api.html#react.rendercomponenttostring

https://github.com/facebook/react/blob/master/src/browser/server/ReactServerRendering.js

jsxTransformer

https://github.com/reactjs/express-react-views/blob/master/index.js
https://github.com/reactjs/React.NET/blob/master/src/React/ReactComponent.cs
http://reactjs.net/guides/server-side-rendering.html
https://github.com/mhart/react-server-example
https://github.com/ssorallen/react-play


http://augustl.com/blog/2014/jdk8_react_rendering_on_server/
http://yanns.github.io/blog/2014/03/15/server-side-rendering-for-javascript-reactjs-framework/

# Monitoring support

# Request & Response
https://github.com/perwendel/spark/blob/master/src/main/java/spark/Request.java
http://rubydoc.info/github/rack/rack/master/Rack/Request
http://expressjs.com/4x/api.html


# Write more documents

* make site on s3
    * define deployment pipeline. 
* more examples
* javadoc


# database integration

https://github.com/brianm/jdbi

# don't work

# template engine support
## Handlebars.java
https://github.com/jknack/handlebars.java

## FreeMarker
http://freemarker.org/

## Thymeleaf
http://www.thymeleaf.org/

## Mustache
https://github.com/spullara/mustache.java

## Jade
https://github.com/neuland/jade4j


## NestedQuery support
[Rack](https://github.com/rack/rack/blob/master/lib/rack/utils.rb#L104) や[qs](https://github.com/hapijs/qs)のようなnested queryはパラメータのキー表現とそれを取り出すときのコードの表現が一致しているから使い易いのであって、Javaでやるとどうしても全然違った表現になってしまう為、特に使い易くない。

やるならJAX-RSのようにパラメータをオブジェクトにマッピングすべき。
JSONからオブジェクトへのマッピングは便利なライブラリが沢山あるのであるからして、NestedQueryを使いたいケースは少ない気がする。



```java
public class NestedQuery {

	Map<String, NestedQuery> kids = new HashMap<>();

	String value = "";

	List<String> list = new ArrayList<>();

	public NestedQuery get(String key) {
		return this.kids.get(key);
	}

	public String value() {
		return this.value;
	}

	public List<String> list() {
		return Collections.unmodifiableList(this.list);
	}

	public static NestedQuery to(Map<String, Deque<String>> params) {
		return to(params, Config.defaults().getMap());
	}

	public static NestedQuery to(Map<String, Deque<String>> params,
			OptionMap options) {
		return null;
	}
}
```

