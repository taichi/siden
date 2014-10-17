
# Monitoring support

# Write more documents

* make site on s3
    * define deployment pipeline. 
* more examples
* javadoc

# database integration

* https://github.com/jOOQ/jOOQ
* https://github.com/brianm/jdbi

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

