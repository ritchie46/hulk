# Hulk DoS tool

Ported from Go/ Python to Scala. [Original Python reference](https://github.com/grafov/hulk/blob/master/hulk.py),
written by Barry Shteiman as educational example. 

Python isn't build for concurrency. The original script opens many threads to spam the endpoint 
with http requests. Golang and Scala have abstracted concurrency in goroutines and futures respectively.
Both are able to do more concurrently using less resources. 

## Modifications
HULK tries to exhaust the target's resources by making many unique http requests. Because every request seems to be 
unique, the target isn't able to fetch the requests from cache and compute is wasted for every request. Despite the
headers and request parameters being unique, the original HULK tool can easily be fingerprinted as explained in 
[this post](https://www.trustwave.com/en-us/resources/blogs/spiderlabs-blog/hulk-vs-thor-application-dos-smackdown/).
The headers of the request are always ordered in the same manner. This implementation shuffles the headers order, 
leaving less of a fingerprint.

## Usage
* Required: **java >= 1.8**

Download the `jar` from the [release page](https://github.com/ritchie46/hulk/releases/), and run the jar with the 
required *url:port*.

```
$ java -jar hulk-0.1.jar --url http://localhost:8080/
```

## Options
```
HULK Unbearable Load King
Usage: HULK [options]

  -u, --url <value>
  -m, --max-process <value>
                           Upper parallel requests limit
  -t, --timeout <value>    Connection timeout duration in ms
  --help
```